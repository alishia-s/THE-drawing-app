package com.the.drawingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment() {
    private val userViewModel by viewModels<UserViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return ComposeView(requireContext()).apply {
            setContent {
                userViewModel.logout()
                LoginScreen(userViewModel)
            }
        }
    }

    @Composable
    fun LoginScreen(userViewModel: UserViewModel = viewModel()) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisibility by remember { mutableStateOf(false) }
        val user = userViewModel.user.collectAsState()
        val authMessage = userViewModel.authMessage.collectAsState(initial = null)

        if (user.value != null) {
            LaunchedEffect(user.value) {
                findNavController().navigate(R.id.action_LoginFragmentToMainScreen)
                Toast.makeText(requireContext(), authMessage.value ?: "Success", Toast.LENGTH_LONG)
                    .show()
            }
        }

        //Color Palette
        val lightestColor = Color(red = 0xD5, green = 0xEF, blue = 0xF7)
        val lighterColor = Color(red = 0xAB, green = 0xDE, blue = 0xED)
        val midColor = Color(red = 0x65, green = 0xB2, blue = 0xD4)
        val darkerColor = Color(red = 0x00, green = 0x77, blue = 0xB6)
        val darkestColor = Color(red = 0x03, green = 0x04, blue = 0x5E)



        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(darkestColor, midColor),
                        start = Offset.Zero,
                        end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
            verticalArrangement = Arrangement.Center
        )
        {
            AppLogoAndTitle()
            EmailEntry(
                email,
                onEmailChange = { email = it },
                lighterColor,
                lightestColor,
                midColor
            )
            PasswordEntry(
                password,
                onPasswordChange = { password = it },
                passwordVisibility,
                onPasswordVisibilityChange = { passwordVisibility = it },
                lighterColor,
                lightestColor,
                midColor
            )
            LoginButton(
                email, password, lightestColor, darkestColor, midColor, lighterColor
            )
            SignUpButton(
                email,
                password,
                darkestColor,
                lighterColor
            )
            if (authMessage.value != null) {
                Text(
                    text = authMessage.value!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }


        }
        InformationSection(darkerColor, darkestColor)
    }


    @Composable
    fun AppLogoAndTitle() {
        Image(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = "App Logo",
            contentScale = ContentScale.Fit, // This is to preserve the aspect ratio of the image
            modifier = Modifier.size(210.dp)
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Text(
            text = "Drawing App",
            fontSize = 52.sp,
            fontWeight = FontWeight(1000),
            letterSpacing = 1.sp,
            color = Color.White,
            modifier = Modifier
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EmailEntry(
        email: String,
        onEmailChange: (String) -> Unit,
        lighterColor: Color,
        lightestColor: Color,
        midColor: Color
    ) {
        Column {
            Text(
                text = "Email ID",
                fontSize = 15.sp,
                fontWeight = FontWeight(1000),
                color = Color.White,
                modifier = Modifier.padding(top = 30.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = midColor
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = midColor,
                    unfocusedBorderColor = lighterColor,
                    cursorColor = midColor
                ),
                placeholder = {
                    Text(
                        text = "Type your email ID",
                        fontWeight = FontWeight(1000),
                        letterSpacing = 1.sp,
                        color = lighterColor,
                    )
                },
                modifier = Modifier
                    .width(280.dp)
                    .padding(top = 5.dp)
                    .background(color = lightestColor, shape = RoundedCornerShape(15.dp)),
                shape = RoundedCornerShape(15.dp)
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PasswordEntry(
        password: String,
        onPasswordChange: (String) -> Unit,
        passwordVisibility: Boolean,
        onPasswordVisibilityChange: (Boolean) -> Unit,
        lighterColor: Color,
        lightestColor: Color,
        midColor: Color
    ) {
        Column {
            Text(
                text = "Password",
                fontSize = 15.sp,
                fontWeight = FontWeight(1000),
                color = Color.White,
                modifier = Modifier.padding(top = 30.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = {
                    Text(
                        text = "Type your password",
                        fontWeight = FontWeight(1000),
                        letterSpacing = 1.sp,
                        color = lighterColor
                    )
                },
                singleLine = true,

                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = midColor,
                    unfocusedBorderColor = lighterColor,
                    cursorColor = midColor
                ),

                visualTransformation = if (passwordVisibility) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },

                trailingIcon = {
                    val image = if (passwordVisibility) {
                        Icons.Outlined.Done
                    } else {
                        Icons.Outlined.Close
                    }
                    IconButton(onClick = { onPasswordVisibilityChange(!passwordVisibility) }) {
                        Icon(image, "Toggle Hide Password")
                    }
                },

                modifier = Modifier
                    .width(280.dp)
                    .padding(top = 5.dp)
                    .background(color = lightestColor, shape = RoundedCornerShape(15.dp)),
                shape = RoundedCornerShape(15.dp)
            )

        }
    }

    @Composable
    fun LoginButton(
        email: String,
        password: String,
        lightestColor: Color,
        darkestColor: Color,
        midColor: Color,
        lighterColor: Color
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = { userViewModel.login(email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(darkestColor, midColor),
                        start = Offset.Zero,
                        end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
        ) {
            Text(
                text = "Sign in",
                fontWeight = FontWeight(1000),
                letterSpacing = 1.sp,
                color = lightestColor,
                fontSize = 22.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 6.dp, start = 22.dp, end = 22.dp)
            )
        }
    }

    @Composable
    fun SignUpButton(email: String, password: String, darkestColor: Color, lighterColor: Color) {
        Spacer(modifier = Modifier.height(40.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = "Don't have an account? ",
                color = lighterColor,
                fontWeight = FontWeight(1000),
                letterSpacing = (3 / 5).sp
            )
            Button(
                onClick = { userViewModel.signUp(email, password) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = darkestColor
                ),

                ) {
                Text(
                    text = "Sign Up",
                    color = Color.White,
                    fontWeight = FontWeight(1000),
                    letterSpacing = (1 / 2).sp
                )
            }
        }
    }

    @Composable
    fun InformationSection(darkerColor: Color, darkestColor: Color) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        ) {
            Text(
                text = "@THE Team - 2024",
                color = darkestColor,
                fontWeight = FontWeight(1000),
                letterSpacing = (1 / 2).sp,
                fontSize = 8.sp
            )
            Text(
                text = "        Powered by Google's Firebase",
                color = darkerColor,
                fontWeight = FontWeight(1000),
                letterSpacing = (1 / 2).sp,
                fontSize = 8.sp
            )
        }
    }
}