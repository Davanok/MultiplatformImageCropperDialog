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

