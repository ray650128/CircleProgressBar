package com.ray650128.circleprogressbar


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View


class CircleProgressBar : View {

    private var diameter = 200  // 直徑
    private var centerX: Float = 0.toFloat()  // 中心點 X 座標
    private var centerY: Float = 0.toFloat()  // 中心點 Y 座標

    // 進度條畫筆
    private var bgLinePaint: Paint? = null
    private var progressPaint: Paint? = null

    // 文字畫筆
    private var vTextPaint: Paint? = null
    private var titlePaint: Paint? = null
    private var subTitlePaint: Paint? = null
    private var unitPaint: Paint? = null
    private var resultPaint: Paint? = null

    private var bgRect: RectF? = null

    //private var progressAnimator: ValueAnimator? = null
    private var mDrawFilter: PaintFlagsDrawFilter? = null

    private val startAngle = 90f
    private var sweepAngle = 360f
    private var currentAngle = 0f
    //private var lastAngle: Float = 0.toFloat()

    private var progressColor = -0x333333
    private var bgArcColor = -0x141414

    private var maxValues = 100f
    private var curValues = -1f
    private var bgArcWidth = dipToPx(2f).toFloat()
    private var progressWidth = dipToPx(10f).toFloat()

    // 字體大小
    private var titleSize = dipToPx(22f).toFloat()
    private var subTitleSize = dipToPx(16f).toFloat()
    private var textSize = dipToPx(52f).toFloat()
    private var unitSize = dipToPx(14f).toFloat()
    private var resultSize = dipToPx(22f).toFloat()


    //private val aniSpeed = 100

    // 字體顏色
    private var titleColor = -0xcccccc
    private var subTitleColor = -0x999999
    private var valueColor = -0xcccccc
    private var unitColor = -0x6c6c6c

    // 文字內容
    private var titleString: String = ""
    private var subTitleString: String = ""
    private var unitString: String = ""
    private var resultString: String = ""

    // 是否顯示小畫面
    private var isMinimal: Boolean = false

    // sweepAngle / maxValues 的值
    private var k: Float = 0.toFloat()

    constructor(context: Context) : super(context, null) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, 0) {
        initConfig(context, attrs)
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initConfig(context, attrs)
        initView()
    }

    /**
     * 初始化配置
     * @param context
     * @param attrs
     */
    private fun initConfig(context: Context, attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar)

        titleColor = a.getColor(R.styleable.CircleProgressBar_titleColor, -0xcccccc)
        subTitleColor = a.getColor(R.styleable.CircleProgressBar_subTitleColor, -0x999999)
        valueColor = a.getColor(R.styleable.CircleProgressBar_valueColor, -0xcccccc)
        unitColor = a.getColor(R.styleable.CircleProgressBar_unitColor, -0x6c6c6c)

        bgArcColor = a.getColor(R.styleable.CircleProgressBar_backgroundLineColor, -0x141414)
        progressColor = a.getColor(R.styleable.CircleProgressBar_progressColor, -0x333333)

        titleSize = a.getDimension(R.styleable.CircleProgressBar_titleTextSize, dipToPx(22f).toFloat())
        subTitleSize = a.getDimension(R.styleable.CircleProgressBar_subTitleTextSize, dipToPx(16f).toFloat())
        textSize = a.getDimension(R.styleable.CircleProgressBar_valueTextSize, dipToPx(52f).toFloat())
        unitSize = a.getDimension(R.styleable.CircleProgressBar_unitTextSize, dipToPx(14f).toFloat())
        resultSize = a.getDimension(R.styleable.CircleProgressBar_resultTextSize, dipToPx(22f).toFloat())

        progressWidth = a.getDimension(R.styleable.CircleProgressBar_progressWidth, dipToPx(12f).toFloat())
        bgArcWidth = a.getDimension(R.styleable.CircleProgressBar_backgroundWidth, dipToPx(5f).toFloat())

        titleString = a.getString(R.styleable.CircleProgressBar_textTitle) ?: ""
        subTitleString = a.getString(R.styleable.CircleProgressBar_textSubTitle) ?: ""
        unitString = a.getString(R.styleable.CircleProgressBar_textUnit) ?: ""
        resultString = a.getString(R.styleable.CircleProgressBar_textResult) ?: ""

        curValues = a.getFloat(R.styleable.CircleProgressBar_currentValue, -1f)
        maxValues = a.getFloat(R.styleable.CircleProgressBar_maxValue, 100f)

        isMinimal = a.getBoolean(R.styleable.CircleProgressBar_isMinimal, false)

        setCurrentValues(curValues)
        setMaxValues(maxValues)
        a.recycle()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        diameter = (Math.min(w, h) - 2 * (progressWidth / 2)).toInt()
        //Log.e("diameter", "$diameter")

        // 進度條區域
        bgRect = RectF()
        bgRect!!.top = progressWidth / 2
        bgRect!!.left = progressWidth / 2
        bgRect!!.right = diameter + (progressWidth / 2)
        bgRect!!.bottom = diameter + (progressWidth / 2)

        // 圓心
        centerX = (2 * (progressWidth / 2) + diameter) / 2
        centerY = (2 * (progressWidth / 2) + diameter) / 2

        //invalidate()
    }

    private fun initView() {
        //背景圓圈
        bgLinePaint = Paint()
        bgLinePaint!!.isAntiAlias = true
        bgLinePaint!!.style = Paint.Style.STROKE
        bgLinePaint!!.strokeWidth = bgArcWidth
        bgLinePaint!!.color = bgArcColor
        bgLinePaint!!.strokeCap = Paint.Cap.ROUND

        //目前進度
        progressPaint = Paint()
        progressPaint!!.isAntiAlias = true
        progressPaint!!.style = Paint.Style.STROKE
        progressPaint!!.strokeCap = Paint.Cap.ROUND
        progressPaint!!.strokeWidth = progressWidth
        progressPaint!!.color = progressColor

        //數值
        vTextPaint = Paint()
        vTextPaint!!.textSize = textSize
        vTextPaint!!.color = valueColor
        vTextPaint!!.textAlign = Paint.Align.CENTER

        //單位
        unitPaint = Paint()
        unitPaint!!.textSize = unitSize
        unitPaint!!.color = unitColor
        unitPaint!!.textAlign = Paint.Align.CENTER

        //標題
        titlePaint = Paint()
        titlePaint!!.textSize = titleSize
        titlePaint!!.color = titleColor
        titlePaint!!.textAlign = Paint.Align.CENTER

        //子標題
        subTitlePaint = Paint()
        subTitlePaint!!.textSize = subTitleSize
        subTitlePaint!!.color = subTitleColor
        subTitlePaint!!.textAlign = Paint.Align.CENTER

        //結果
        resultPaint = Paint()
        resultPaint!!.textSize = resultSize
        resultPaint!!.color = progressColor
        resultPaint!!.textAlign = Paint.Align.CENTER

        mDrawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    }

    override fun onDraw(canvas: Canvas) {
        // 反鋸齒
        canvas.drawFilter = mDrawFilter

        // 背景圓圈
        canvas.drawArc(bgRect!!, startAngle, sweepAngle, false, bgLinePaint!!)

        // 進度條
        canvas.drawArc(bgRect!!, startAngle, currentAngle, false, progressPaint!!)

        // 數值
        if (curValues < 0) {
            canvas.drawText("--", centerX, centerY + textSize / 3, vTextPaint!!)
        } else {
            canvas.drawText(String.format("%.0f", curValues), centerX, centerY + textSize / 3, vTextPaint!!)
        }

        // 文字方塊
        if (!isMinimal) {
            canvas.drawText(titleString, centerX, centerY - 3.25f * textSize / 3, titlePaint!!)
            canvas.drawText(subTitleString, centerX, centerY - 2f * textSize / 3, subTitlePaint!!)

            canvas.drawText(unitString, centerX, centerY + 2.25f * textSize / 3, unitPaint!!)

            if (curValues < 0) {
                resultPaint?.color = -0x333333
                canvas.drawText("--", centerX, centerY + 3.75f * textSize / 3, resultPaint!!)
            } else {
                canvas.drawText(resultString, centerX, centerY + 3.75f * textSize / 3, resultPaint!!)
            }
        } else {
            canvas.drawText(titleString, centerX, centerY - 3.5f * textSize / 3, titlePaint!!)
            canvas.drawText(unitString, centerX, centerY + 4f * textSize / 3, unitPaint!!)
        }

        invalidate()
    }

    /**
     * 設定最大值
     * @param maxValues
     */
    fun setMaxValues(maxValues: Float) {
        this.maxValues = maxValues
        k = sweepAngle / maxValues
    }

    /**
     * 目前進度
     * @param currentValues
     */
    fun setCurrentValues(currentValues: Float) {
        var mValue = currentValues
        if (mValue > maxValues) {
            mValue = maxValues
        }
        /*if (mValue < 0) {
            mValue = 0f
        }*/
        this.curValues = mValue
        currentAngle = if (mValue < 0) {
            0f
        } else {
            (mValue / maxValues) * 360f
        }
        //lastAngle = currentAngle
        //setAnimation(lastAngle, mValue * k, aniSpeed)
    }

    /**
     * 設定進度條顏色
     * @param color
     */
    fun setProgressColor(color: Int) {
        this.progressColor = color

        this.progressPaint?.color = color
        this.resultPaint?.color = color
        invalidate()
    }

    /**
     * 設定背景圓圈寬度
     * @param bgArcWidth
     */
    fun setBgArcWidth(bgArcWidth: Int) {
        this.bgArcWidth = bgArcWidth.toFloat()
    }

    /**
     * 設定進度條寬度
     * @param progressWidth
     */
    fun setProgressWidth(progressWidth: Int) {
        this.progressWidth = progressWidth.toFloat()
    }


    /*-----------[ 設定文字大小 ]-----------*/
    /**
     * 設定標題文字大小
     * @param textSize
     */
    fun setTitleSize(textSize: Int) {
        this.titleSize = textSize.toFloat()
    }

    /**
     * 設定子標題文字大小
     * @param textSize
     */
    fun setSubTitleSize(textSize: Int) {
        this.subTitleSize = textSize.toFloat()
    }

    /**
     * 設定數值文字大小
     * @param textSize
     */
    fun setTextSize(textSize: Int) {
        this.textSize = textSize.toFloat()
    }

    /**
     * 設定單位文字大小
     * @param textSize
     */
    fun setUnitSize(textSize: Int) {
        this.unitSize = textSize.toFloat()
    }

    /**
     * 設定結果文字大小
     * @param textSize
     */
    fun setResultSize(textSize: Int) {
        this.resultSize = textSize.toFloat()
    }


    /*-----------[ 設定文字 ]-----------*/
    /**
     * 設定標題文字
     * @param text
     */
    fun setTitle(text: String) {
        this.titleString = text
    }

    /**
     * 設定子標題文字
     * @param text
     */
    fun setSubTitle(text: String) {
        this.subTitleString = text
    }

    /**
     * 設定單位文字
     * @param unitString
     */
    fun setUnit(unitString: String) {
        this.unitString = unitString
        //invalidate()
    }

    /**
     * 設定結果文字
     * @param text
     */
    fun setResultText(text: String) {
        this.resultString = text
    }

    /**
     * 進度條移動動畫
     * @param last
     * @param current
     */
    /*private fun setAnimation(last: Float, current: Float, length: Int) {
        progressAnimator = ValueAnimator.ofFloat(last, current)
        progressAnimator!!.duration = length.toLong()
        progressAnimator!!.setTarget(currentAngle)
        progressAnimator!!.addUpdateListener { animation ->
            currentAngle = animation.animatedValue as Float
            curValues = currentAngle / k
        }
        progressAnimator!!.start()
    }*/

    /**
     * dip 轉 px
     * @param dip
     * @return
     */
    private fun dipToPx(dip: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dip * density + 0.5f * if (dip >= 0) 1 else -1).toInt()
    }
}