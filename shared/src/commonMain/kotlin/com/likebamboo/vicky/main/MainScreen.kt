package com.likebamboo.vicky.main


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun MainScreen(viewModel: MainViewModel = MainViewModel()) {

    val uiState by viewModel.uiState.collectAsState()

    val propertyFilePicker = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("xls", "xlsx")),
        mode = PickerMode.Single,
        title = "选择配置文件",
        onResult = { f ->
            f?.let {
                viewModel.onEvent(MainEvent.LoadPropertyFile(f.file))
            }
        },
    )
    val mainFilePicker = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("xls", "xlsx")),
        mode = PickerMode.Single,
        title = "选择主文件",
        onResult = { f ->
            f?.let {
                viewModel.onEvent(MainEvent.LoadDataFile(f.file))
            }
        })

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = uiState.propertyFile?.absolutePath ?: "",
                onValueChange = {},
                label = { Text("配置文件路径") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true,
                trailingIcon = {
                    Button(
                        onClick = { propertyFilePicker.launch() },
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text("选择配置文件")
                    }
                })
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.dataFile?.absolutePath ?: "",
                onValueChange = {},
                label = { Text("数据文件路径") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true,
                trailingIcon = {
                    Button(
                        onClick = { mainFilePicker.launch() },
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text("选择数据文件")
                    }
                })
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        viewModel.onEvent(MainEvent.Submit(""))
                    },
                    enabled = uiState.dataFile != null && uiState.propertyFile != null && !uiState.loading,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text("提交", modifier = Modifier.padding(horizontal = 30.dp))
                }
            }
            if (uiState.error?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(uiState.error ?: "")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(4.dp)
            ) {
                Text(
                    text = uiState.log.toString(),
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        // Loading 覆盖层
        if (uiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
