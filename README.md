# 간단한 녹음기

### 권한요청, 권한 수동 설정
~~~kotlin
// 권한요청 (RECORD_AUDIO)
private val requiredPermissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
requestPermissions(requiredPermissions, CODE)
// 권한 결과? (onRequestPermissionsResult)
override fun onRequestPermissionsResult(
	requestCode: Int,
	permissions: Array<out String>,
	grantResults: IntArray
) {
	super.onRequestPermissionsResult(requestCode, permissions, grantResults)
	if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
			grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
		// 정상적으로 권한이 승인
	}
	else {
		// 권한이 승인되지않음
		Toast.makeText(this, "음성녹음 권한이 반드시 필요합니다.", Toast.LENGTH_SHORT).show()
		// 이 앱의 설정화면을 띄워주고 앱은 종료 (수동으로 권한설정)
		val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:$packageName"))
		startActivity(intent)
		finish()
	}
}
~~~

### 사용자정의 Button
~~~kotlin
// RecordButton.kt 생성
class RecordButton(
	context: Context,
	attrs: AttributeSet
): AppCompatImageButton(context, attrs) {
	// 클래스 생성 시에 배경 설정(1회)
	init {
		setBackgroundResource(R.drawable.background)
	}
	// state에 따라 imageButton의 image 변경
	fun updateIconWithState(state: State) {
		when(state) {
			State.BEFORE_RECORDING -> {
				setImageResource(R.drawable.ic_record)
			}
			State.ON_RECODING -> {
				setImageResource(R.drawable.ic_stop)
			}
		}
	}
}
~~~

### 사용자정의 View
~~~ kotlin
class SoundVisualizerView(
	context: Context,
	attrs: AttributeSet? = null
): View(context, attrs) {

	override fun onSizeChanged
	(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
	// View의 가로, 세로를 가져올 수 있음
	drawingWidth = w
	drawingHeight = h
	}

	// 그려줄 라인의 속성을 정의 (Paint)
	private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		color = context.getColor(android.R.color.holo_orange_light)
		strokeWidth = LINE_WIDTH
		strokeCap = Paint.Cap.ROUND

	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)

	canvas ?: return
	// (x, y) 좌표로 라인을 그려줄 수 있음
	canvas.drawLine(startX, startY, endX, endY, paint)
	// paint는 동작이 크기때문에 onDraw 바깥에서 정의
	// onDraw는 반복적으로 호출되기 때문에
	}
}

invalidate() // onDraw를 호출함
~~~

### Runnable, handler, post, removeCallbacks
~~~kotlin
// Runnable : 수행할 동작을 정의
private val Action: Runnable = object: Runnable {
	override fun run() {

		if (!isReplaying) {
			val currentAmplitude = onRequestCurrentAmplitude?.invoke() ?: 0
			drawingAmplitudeList = listOf(currentAmplitude) + drawingAmplitudeList
		}
		else {
			replayingPosition++
		}
		invalidate() // onDraw를 호출함
		// 자신을 ACTION_INTERVAL(20) 주기로 반복 호출
		handler?.postDelayed(this, ACTION_INTERVAL)
	}
}
// Action을 실행
handler?.post(Action)
// Action 실행을 취소
handler?.removeCallbacks(Action)
~~~