# File Manager

This is a simple file manager app built using Kotlin in Android Studio. The app allows users to browse files on their Android device.

## Features

- View files and folders on the device.
- Navigate into folders and subfolders.
- Sort files by their name, size, time of creation, or extension.
- Tap on a file to open it in an appropriate application.
- Long-press on a file to share it.
- Image file preview.
- Files that have been changed since the last time you've seen them are displayed with a blue background.

## Screenshots

![1](https://i.ibb.co/C0KH904/1.png) | ![2](https://i.ibb.co/VtkQWsp/2.png)
:-----------------------------------:|:------------------------------------:
![3](https://i.ibb.co/t4GZGCx/3.png) | ![4](https://i.ibb.co/tZskTgC/4.png)

## Getting Started

To get started with the app, follow these steps:

1. Clone this repository to your local machine.
2. Open the project in Android Studio.
3. Ensure that your Android SDK and build tools match the version specified in the `build.gradle` file.
4. Build and run the app on an Android device or emulator.

Note that the app requires permission to access files on the device, so it will prompt the user to grant permission on first launch. If the user denies permission, the app will close.

## Usage

To use the app, simply open it and navigate to the desired folder using the file browser. The files in the folder will be displayed in a RecyclerView, sorted by name by default. You can sort the files using the sorting menu in the app bar.

If a file name is too long to fit in the available space, it will be truncated with an ellipsis to preserve as much information as possible.

Tap on a file to open it in an appropriate application. To share a file, long-press on it to open the system share dialog. From there, you can select the app you want to use to share the file.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.
