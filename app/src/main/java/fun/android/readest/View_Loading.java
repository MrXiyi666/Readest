package fun.android.readest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class View_Loading extends View {
    Paint paint;
    public void init(){
        paint = new Paint();
        paint.setColor(Color.WHITE);

    }

    public View_Loading(Context context) {
        super(context);
        init();
    }

    public View_Loading(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public View_Loading(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(50, 50, this.getWidth() / 2, paint);
    }
}
