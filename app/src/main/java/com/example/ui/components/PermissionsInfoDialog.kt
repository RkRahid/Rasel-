package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisDeepDark
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun PermissionsInfoDialog(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, JarvisCyan, RoundedCornerShape(20.dp)),
            color = JarvisDeepDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "◆ RAHID JARVIS AI // সিস্টেম নির্দেশিকা",
                    color = JarvisCyanBright,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                SectionHeader("১. পারমিশন নির্দেশিকা (Android Security & Privacy)")
                PermissionItem(
                    title = "🎙️ মাইক্রোফোন (Record Audio)",
                    desc = "বাংলা ও ইংরেজি ভয়েস কমান্ড শোনার জন্য প্রয়োজন। আপনি যেকোনো সময় সিস্টেম সেটিংসে এটি নিয়ন্ত্রণ করতে পারেন।"
                )
                PermissionItem(
                    title = "🔦 ক্যামেরা / ফ্ল্যাশলাইট (Camera Hardware)",
                    desc = "শুধুমাত্র ফোনের টর্চ লাইট অন/অফ করার জন্য ব্যবহৃত হয়। কোনো ছবি বা ভিডিও সংরক্ষণ করা হয় না।"
                )
                PermissionItem(
                    title = "🔒 অ্যাক্সেসিবিলিটি ও নোটিফিকেশন এক্সেস (Optional)",
                    desc = "সম্পূর্ণ ঐচ্ছিক! অ্যাপটি কোনো লকস্ক্রিন বা নিরাপত্তা বাইপাস করে না। সংবেদনশীল কাজের আগে সবসময় আপনার সম্মতি চাওয়া হয়।"
                )

                HorizontalDivider(
                    color = JarvisCyan.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                SectionHeader("২. অফলাইন বনাম অনলাইন ফিচার (Offline vs Online)")
                FeatureItem("⚡ অফলাইন ফিচার (ইন্টারনেট ছাড়া চলবে):", "টর্চ লাইট অন/অফ, বর্তমান সময় ও তারিখ, ইউটিউব/ক্যালকুলেটর/ক্যামেরা/সেটিংস অ্যাপ ওপেন, ডিভাইস ডায়াগনস্টিকস।")
                FeatureItem("🌐 অনলাইন ফিচার (ইন্টারনেট প্রয়োজন):", "Google Gemini 3.5 AI এর সাথে জটিল প্রশ্নের উত্তর, জ্ঞানকোষ, যুক্তি, কোডিং এবং সাধারণ চ্যাট।")

                HorizontalDivider(
                    color = JarvisCyan.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                SectionHeader("৩. বাংলা স্পিচ ইঞ্জিন (Bengali Voice Pack)")
                Text(
                    text = "বাংলা মুখে শোনার জন্য ফোনে 'Google Speech Services' বা 'Google TTS' এ বাংলা (বাংলাদেশ) ভয়েস প্যাক ইনস্টল থাকলে সেরা অভিজ্ঞতা পাওয়া যাবে।",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_info_dialog_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "বুঝেছি (Understood)",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = JarvisCyan,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun PermissionItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            color = JarvisTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = desc,
            color = JarvisTextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun FeatureItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            color = JarvisCyanBright,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = desc,
            color = JarvisTextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}
