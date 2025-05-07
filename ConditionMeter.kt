@Composable
fun ConditionMeter(
    conditions: List<Pair<String, Boolean>>,
    modifier: Modifier = Modifier,
    animationDuration: Int = 1000
) {
    val animatedProgress by animateFloatAsState(
        targetValue = conditions.count { it.second }.toFloat() / conditions.size,
        animationSpec = tween(durationMillis = animationDuration)
    )
    
    Column(modifier = modifier.padding(16.dp)) {
        // Meter with animation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colors.surface.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50), 
                                Color(0xFF8BC34A)
                            )
                        )
                    )
                    .animateContentSize()
            )
            
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Condition list
        LazyColumn {
            items(conditions) { (label, met) ->
                ConditionRow(
                    label = label,
                    met = met,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ConditionRow(
    label: String,
    met: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.Info,
            contentDescription = null,
            tint = if (met) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = if (met) 1f else 0.7f),
            modifier = Modifier.weight(1f)
        )
    }
}