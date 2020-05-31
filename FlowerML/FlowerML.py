import os

import tensorflow as tf
import tensorflow_datasets as tfds
import matplotlib.pyplot as plt
from tensorflow.python.keras.layers import Conv2D, MaxPooling2D, Dropout, Flatten, Dense
from tensorflow.python.keras.regularizers import l2
from tensorflow.python.ops.image_ops_impl import ResizeMethod

os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
image_size = 44
batch_size = 128

# Convert the model.
converter = tf.lite.TFLiteConverter.from_saved_model('flower_model')
tflite_model = converter.convert()
open("flower_model.tflite", "wb").write(tflite_model)

interpreter = tf.lite.Interpreter(model_path="flower_model.tflite")
interpreter.allocate_tensors()

# Print input shape and type
print(interpreter.get_input_details()[0]['shape'])  # Example: [1 224 224 3]
print(interpreter.get_input_details()[0]['dtype'])  # Example: <class 'numpy.float32'>

# Print output shape and type
print(interpreter.get_output_details()[0]['shape'])  # Example: [1 1000]
print(interpreter.get_output_details()[0]['dtype'])  # Example: <class 'numpy.float32'>

ds_train, ds_validation = tfds.load(
    'oxford_flowers102',
    split=['train+test+validation[:50%]', 'validation[-50%:]'],
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

"""
ds_test = ds_test.map(
    normalize_img, num_parallel_calls=tf.data.experimental.AUTOTUNE)
ds_test = ds_test.batch(batch_size)
ds_test = ds_test.cache()
ds_test = ds_test.prefetch(tf.data.experimental.AUTOTUNE)
"""

ds_validation = ds_validation.map(
    normalize_img, num_parallel_calls=tf.data.experimental.AUTOTUNE)
ds_validation = ds_validation.batch(batch_size)
ds_validation = ds_validation.cache()
ds_validation = ds_validation.prefetch(tf.data.experimental.AUTOTUNE)

model = tf.keras.models.Sequential([
    Conv2D(32, (2, 2), activity_regularizer=l2(0.0013), activation='relu', input_shape=(image_size, image_size, 3)),
    MaxPooling2D(2, 2),
    Dropout(0.5),
    Conv2D(64, (3, 3), activity_regularizer=l2(0.001), activation='relu'),
    MaxPooling2D(2, 2),
    Dropout(0.5),
    Flatten(),
    Dense(256, activity_regularizer=l2(0.002), activation='relu'),
    Dropout(0.6),
    Dense(102, activation='softmax'),
])
model.summary()

model.compile(
    loss='sparse_categorical_crossentropy',
    optimizer=tf.keras.optimizers.Adam(0.001),
    metrics=['accuracy'],
)

history = model.fit(
    ds_train,
    epochs=125,
    validation_data=ds_validation,
)

plt.plot(history.history['accuracy'])
plt.plot(history.history['val_accuracy'])
plt.title('model accuracy')
plt.ylabel('accuracy')
plt.xlabel('epoch')
plt.legend(['train', 'test'], loc='upper left')
plt.show()

# Re-evaluate the model
loss, acc = model.evaluate(ds_validation, verbose=2)
print("Restored model, accuracy: {:5.2f}%".format(100*acc))
if acc > 0.6:
    model.save('flower_model')
    print('saved')
