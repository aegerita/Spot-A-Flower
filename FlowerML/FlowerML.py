import os

import tensorflow as tf
import tensorflow_datasets as tfds
from PIL import Image
from tensorflow.python.ops.image_ops_impl import ResizeMethod

os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
image_size = 128

ds_train, ds_test, ds_validation = tfds.load(
    'oxford_flowers102',
    split=['train', 'test', 'validation'],
    shuffle_files=True,
    as_supervised=True,
)


def normalize_img(image, label):
    image = tf.image.resize_with_pad(
        image, image_size, image_size, method=ResizeMethod.BILINEAR,
        antialias=False
    )
    return tf.cast(image, tf.float32) / 255., label


ds_train = ds_train.map(
    normalize_img, num_parallel_calls=tf.data.experimental.AUTOTUNE)
ds_train = ds_train.cache()
ds_train = ds_train.shuffle(1000)
ds_train = ds_train.batch(32)
ds_train = ds_train.prefetch(tf.data.experimental.AUTOTUNE)

ds_test = ds_test.map(
    normalize_img, num_parallel_calls=tf.data.experimental.AUTOTUNE)
ds_test = ds_test.batch(128)
ds_test = ds_test.cache()
ds_test = ds_test.prefetch(tf.data.experimental.AUTOTUNE)

ds_validation = ds_validation.map(
    normalize_img, num_parallel_calls=tf.data.experimental.AUTOTUNE)
ds_validation = ds_validation.batch(128)
ds_validation = ds_validation.cache()
ds_validation = ds_validation.prefetch(tf.data.experimental.AUTOTUNE)


model = tf.keras.models.Sequential([
    tf.keras.layers.Flatten(input_shape=(image_size, image_size, 3)),
    tf.keras.layers.Dense(512, activation='relu'),
    tf.keras.layers.Dense(512, activation='relu'),
    tf.keras.layers.Dense(512, activation='relu'),
    tf.keras.layers.Dense(112, activation='softmax')
])
model.compile(
    loss='sparse_categorical_crossentropy',
    optimizer=tf.keras.optimizers.Adam(0.001),
    metrics=['accuracy'],
)

model.fit(
    ds_train,
    epochs=30,
    validation_data=ds_test,
)


test_accuracy = tf.keras.metrics.Accuracy()
for (x, y) in ds_validation:
    # training=False is needed only if there are layers with different
    # behavior during training versus inference (e.g. Dropout).
    logits = model(x, training=False)
    prediction = tf.argmax(logits, axis=1, output_type=tf.int32)
    test_accuracy(prediction, y)

# print(tf.stack([y, prediction], axis=1))
print("Test set accuracy: {:.3%}".format(test_accuracy.result()))

"""
pic = Image.open('zonal_geranium_pink_annual_bhs_18-1.jpg')
test_pic = pic.resize((image_size, image_size))
test_pic.show()

tf.fromPixels(test_pic)
predictions = model(test_pic, training=False)
print(predictions)
"""

model.save('flower_model')
