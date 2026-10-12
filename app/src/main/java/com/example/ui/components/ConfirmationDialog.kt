package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.JarvisAccentRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepDark
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String = "অনুমতি দিন (Confirm)",
    cancelButtonText: String = "বাতিল (Cancel)",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, if (isDestructive) JarvisAccentRed else JarvisCyan, RoundedCornerShape(20.dp)),
            color = JarvisDeepDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "◆ $title",
                    color = if (isDestructive) JarvisAccentRed else JarvisCyan,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    color = JarvisTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_cancel_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = cancelButtonText,
                            color = JarvisTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(1.dp).padding(horizontal = 4.dp))

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.testTag("dialog_confirm_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDestructive) JarvisAccentRed else JarvisCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = confirmButtonText,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
