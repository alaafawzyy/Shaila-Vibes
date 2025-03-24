package com.example.shailavibes.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shailavibes.R
import com.example.shailavibes.ui.theme.Green

@Composable
fun DrawerContent(
    onFilterChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
        Column(
            modifier = modifier
                .fillMaxHeight()
                .background(Color(0xFF2f3841))
        ) {
          Box(
              modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(bottomEnd = 30.dp, bottomStart = 30.dp))
                  .background(Color(0xFF2a333c))

          ) {
           Column() {
               Image(
                   painter= painterResource(com.example.shailavibes.R.drawable.logo),
                   contentDescription = "logo",
                   Modifier
                       .size(150.dp)
                       .padding(start = 16.dp, end = 16.dp, top = 16.dp))
                Text(
                    text = stringResource(com.example.shailavibes.R.string.drawer_title),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top =8.dp, start = 16.dp, end = 16.dp, bottom = 18.dp)
                )
            }}

                    DrawerItem(
                        icon = ImageVector.vectorResource(id = com.example.shailavibes.R.drawable.ic_list),
                        text = stringResource(R.string.drawer_item_all_list),
                        onClick = { onFilterChange(false) }
                    )

            DrawerItem(
                icon = ImageVector.vectorResource(id = com.example.shailavibes.R.drawable.ic_favorite),
                text = stringResource(R.string.drawer_item_fav_list),
                onClick = { onFilterChange(true) }
            )
        }
    }


@Composable
fun DrawerItem(
    icon: ImageVector ,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint =  Green,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}