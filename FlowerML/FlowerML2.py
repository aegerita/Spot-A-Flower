import os

import tensorflow as tf
import tensorflow_datasets as tfds
import tensorflow_hub as hub
from tensorflow.keras import layers
from tensorflow.python.keras.layers.preprocessing.image_preprocessing import ResizeMethod
from tensorflow.python.keras.regularizers import l1

os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
image_size = 224
batch_size = 128

converter = tf.lite.TFLiteConverter.from_saved_model('flower_model')
tflite_model = converter.convert()
open("flower_model.tflite", "wb").write(tflite_model)

ds_train, ds_validation = tfds.load(
    'oxford_flowers102',
    split=['train+test+validation', 'validation'],
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
ds_validation = ds_validation.map(
    normalize_img, num_parallel_calls=tf.data.experimental.AUTOTUNE)
ds_validation = ds_validation.batch(batch_size)
ds_validation = ds_validation.cache()
ds_validation = ds_validation.prefetch(tf.data.experimental.AUTOTUNE)


# Create a Feature Extractor
URL = "https://tfhub.dev/google/tf2-preview/mobilenet_v2/feature_vector/4"
feature_extractor = hub.KerasLayer(URL, input_shape=(image_size, image_size, 3))
# Freeze the Pre-Trained Model
feature_extractor.trainable = False
# Attach a classification head
model = tf.keras.Sequential([
  feature_extractor,
  layers.Dense(102, activity_regularizer=l1(), activation='softmax')
])

model.compile(
    loss='sparse_categorical_crossentropy',
    optimizer=tf.keras.optimizers.Adam(),
    metrics=['accuracy'],
)

history = model.fit(
    ds_train,
    epochs=10,
    validation_data=ds_validation
)

loss, acc = model.evaluate(ds_validation, verbose=2)
print("Restored model, accuracy: {:5.2f}%".format(100 * acc))
if acc > 0.89:
    model.save('flower_model')
    print('saved')
