package com.example.shailavibes.ui

import com.example.shailavibes.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shailavibes.ui.data.DrawerItem

@Composable
fun DrawerContent(onItemClick: () -> Unit) {
    val drawerItems = listOf(
        DrawerItem("كل الشيلات",R.drawable.ic_music),
        DrawerItem("قائمة المفضلة",R.drawable.ic_favorite),

    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .background(Color(0xFF3a444e))

    ) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                //.height(130.dp)
                .background(Color(0xFF2b343d)
                ),
        ) {
             Image(painter =painterResource( id=R.drawable.logo),
                 contentDescription = "Logo", modifier = Modifier.size(100.dp).padding(top = 30.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "منوعات شيلات الطريق",
                color = Color(0xFFFDA400),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
        LazyColumn {
            items(drawerItems) { item ->
                DrawerItemRow(item = item, onClick = onItemClick)
            }
        }
    }
}

@Composable
fun DrawerItemRow(item: DrawerItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = item.icon),
            contentDescription = "Heart Icon",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium

        )
    }
}