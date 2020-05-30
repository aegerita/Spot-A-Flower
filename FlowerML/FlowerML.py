import os

import tensorflow as tf
import tensorflow_datasets as tfds
import matplotlib.pyplot as plt
from tensorflow.python.keras.layers import Conv2D, MaxPooling2D, Dropout, Flatten, Dense
from tensorflow.python.keras.regularizers import l2
from tensorflow.python.ops.image_ops_impl import ResizeMethod

os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
image_size = 40
batch_size = 64

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
ds_train = ds_train.batch(batch_size)
ds_train = ds_train.prefetch(tf.data.experimental.AUTOTUNE)

ds_test = ds_test.map(
    normalize_img, num_parallel_calls=tf.data.experimental.AUTOTUNE)
ds_test = ds_test.batch(batch_size)
ds_test = ds_test.cache()
ds_test = ds_test.prefetch(tf.data.experimental.AUTOTUNE)

ds_validation = ds_validation.map(
    normalize_img, num_parallel_calls=tf.data.experimental.AUTOTUNE)
ds_validation = ds_validation.batch(batch_size)
ds_validation = ds_validation.cache()
ds_validation = ds_validation.prefetch(tf.data.experimental.AUTOTUNE)

model = tf.keras.models.Sequential([
    Conv2D(32, (2, 2), activity_regularizer=l2(0.001), activation='relu', input_shape=(image_size, image_size, 3)),
    MaxPooling2D(2, 2),
    Dropout(0.5),
    Conv2D(64, (3, 3), activity_regularizer=l2(0.001), activation='relu'),
    MaxPooling2D(2, 2),
    Dropout(0.5),
    Flatten(),
    Dense(512, activity_regularizer=l2(0.001), activation='relu'),
    Dropout(0.5),
    Dense(112, activation='softmax'),
])
model.summary()

model.compile(
    loss='sparse_categorical_crossentropy',
    optimizer=tf.keras.optimizers.Adam(0.001),
    metrics=['accuracy'],
)

history = model.fit(
    ds_test,
    epochs=30,
    validation_data=ds_validation,
)

plt.plot(history.history['accuracy'])
plt.plot(history.history['val_accuracy'])
plt.title('model accuracy')
plt.ylabel('accuracy')
plt.xlabel('epoch')
plt.legend(['train', 'test'], loc='upper left')
plt.show()

test_accuracy = tf.keras.metrics.Accuracy()
for (x, y) in ds_train:
    # training=False is needed only if there are layers with different
    # behavior during training versus inference (e.g. Dropout).
    logits = model(x, training=False)
    prediction = tf.argmax(logits, axis=1, output_type=tf.int32)
    test_accuracy(prediction, y)

# print(tf.stack([y, prediction], axis=1))
print("Test set accuracy: {:.3%}".format(test_accuracy.result()))

if test_accuracy.result() > 0.48:
    model.save('flower_model')
    print('saved')
