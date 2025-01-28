## Usage
```kotlin
var byteArray by remember { mutableStateOf<ByteArray?>(null) }
byteArray?.let {
  ImageCropDialog(
    bytes = it,
    boxSize = 300.dp,
    onResult = {
      byteArray = null
      image = it
    }
  )
}
```

## Sample
```kotlin
@Composable
fun SomeComposableFun(

) {
  var file by remember { mutableStateOf<PlatformFile?>(null) }
  var byteArray by remember { mutableStateOf<ByteArray?>(null) }
  val filePicker = rememberFilePickerLauncher(
    type = PickerType.Image
  ) {
    file = it
  }
  val scope = rememberCoroutineScope()
  LaunchedEffect(file) {
    scope.launch {
      byteArray = file?.toByteArray()
    }
  }
  Box {
    Button(
      onClick = { filePicker.launch() }
    ) {
      Text("pick")
    }
  }
  var image by remember { mutableStateOf<ImageBitmap?>(null) }
  byteArray?.let {
    ImageCropDialog(
      byteArray!!,
      300.dp
    ) {
      file = null
      image = it
    }
  }
  image?.let {
    Image(
      bitmap = it,
      contentDescription = null
    )
  }
}
```

![Screenshot 1](https://github.com/user-attachments/assets/3475d0ca-4e41-4a53-b35f-f2273e698389)
![Screenshot 2](https://github.com/user-attachments/assets/f613daeb-6180-4b8d-84d8-18890a65effd)
![Screenshot_3](https://github.com/user-attachments/assets/2484fe39-71bd-4972-baa4-b2cfde43649a)
