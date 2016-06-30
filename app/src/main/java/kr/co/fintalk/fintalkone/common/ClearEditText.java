package kr.co.fintalk.fintalkone.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import kr.co.fintalk.fintalkone.R;

/**
 * Created by BeomyongChoi on 6/29/16
 */
public class ClearEditText extends AppCompatEditText implements TextWatcher, View.OnTouchListener, View.OnFocusChangeListener {

    private Drawable clearDrawable;
    private Drawable wonDrawable;
    private Drawable periodDrawable;
    private Drawable percentDrawable;

    private boolean isWon = false;
    private boolean isPeriod = false;
    private boolean isRate = false;

    private OnFocusChangeListener onFocusChangeListener;
    private OnTouchListener onTouchListener;

    private String mReplaceText = "";

    public void setWon(boolean won) {
        isWon = won;
    }

    public void setPeriod(boolean period) {
        isPeriod = period;
    }

    public void setRate(boolean rate) {
        isRate = rate;
    }

    public ClearEditText(final Context context) {
        super(context);
        init();
    }

    public ClearEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearEditText(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        this.onFocusChangeListener = onFocusChangeListener;
    }

    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }


    private void init() {

        Drawable tempDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_cancel_24dp);
        clearDrawable = DrawableCompat.wrap(tempDrawable);
        clearDrawable.setBounds(15, 0, clearDrawable.getIntrinsicWidth() + 15, clearDrawable.getIntrinsicHeight());

        tempDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_fingerprint_black_24dp);
        wonDrawable = DrawableCompat.wrap(tempDrawable);
        DrawableCompat.setTintList(wonDrawable, getHintTextColors());
        wonDrawable.setBounds(15, 0, wonDrawable.getIntrinsicWidth() + 15, wonDrawable.getIntrinsicHeight());

        tempDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_keyboard_arrow_right_black_24dp);
        periodDrawable = DrawableCompat.wrap(tempDrawable);
        DrawableCompat.setTintList(periodDrawable, getHintTextColors());
        periodDrawable.setBounds(15, 0, periodDrawable.getIntrinsicWidth() + 15, periodDrawable.getIntrinsicHeight());

        tempDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_keyboard_arrow_right_black_24dp);
        percentDrawable = DrawableCompat.wrap(tempDrawable);
        DrawableCompat.setTintList(percentDrawable, getHintTextColors());
        percentDrawable.setBounds(15, 0, percentDrawable.getIntrinsicWidth() + 15, percentDrawable.getIntrinsicHeight());

        setClearIconVisible(false);
        setWonIconVisible(false);
        setPeriodIconVisible(false);
        setPercentIconVisible(false);

        super.setOnTouchListener(this);
        super.setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }


    @Override
    public void onFocusChange(final View view, final boolean hasFocus) {
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
            setWonIconVisible(false);
            setPeriodIconVisible(false);
            setPercentIconVisible(false);
        } else {
            setClearIconVisible(false);
            if (isWon) {
                setWonIconVisible(true);
            } else if (isPeriod) {
                setPeriodIconVisible(true);
            } else if (isRate) {
                setPercentIconVisible(true);
            }
        }

        if (onFocusChangeListener != null) {
            onFocusChangeListener.onFocusChange(view, hasFocus);
        }
    }


    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        final int x = (int) motionEvent.getX();
        if (clearDrawable.isVisible() && x > getWidth() - getPaddingRight() - clearDrawable.getIntrinsicWidth()) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                setError(null);
                setText(null);
            }
            return true;
        }

        return onTouchListener != null && onTouchListener.onTouch(view, motionEvent);

    }

    @Override
    public final void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        if (isFocused()) {
            setClearIconVisible(s.length() > 0);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (isWon) {
            /***
             * No need to continue the function if there is nothing to
             * format
             ***/
            if (s.length() == 0) {
                return;
            }

            /*** Now the number of digits in price is limited to 12 ***/
            String value = s.toString().replaceAll(",", "").replace("원", "");
            if (value.length() > 12) {
                value = value.substring(0, 12);
            }
            String formattedPrice = getFormattedCurrency(value);
            if (!(formattedPrice.equalsIgnoreCase(s.toString()))) {
                /***
                 * The below given line will call the function recursively
                 * and will ends at this if block condition
                 ***/
                setText(formattedPrice);
                setSelection(this.length());
            }
        } else if (isPeriod) {
            if (s.toString().length() > 0) {
                if (Integer.parseInt(s.toString()) > 36) {
                    setText(mReplaceText);
                    setSelection(mReplaceText.length() - 1);
                }
            }
        } else if (isRate) {
            if (s.toString().length() > 0) {
                if (Double.parseDouble(s.toString()) >= 100) {
                    setText(mReplaceText);
                    setSelection(mReplaceText.length() - 1);
                }
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (isPeriod || isRate) {
            mReplaceText = s.toString();
        }
    }

    private void setClearIconVisible(boolean visible) {
        clearDrawable.setVisible(visible, false);
        setCompoundDrawables(null, null, visible ? clearDrawable : null, null);
    }

    private void setWonIconVisible(boolean visible) {
        wonDrawable.setVisible(visible, false);
        setCompoundDrawables(null, null, visible ? wonDrawable : null, null);
    }

    private void setPeriodIconVisible(boolean visible) {
        periodDrawable.setVisible(visible, false);
        setCompoundDrawables(null, null, visible ? periodDrawable : null, null);
    }

    private void setPercentIconVisible(boolean visible) {
        percentDrawable.setVisible(visible, false);
        setCompoundDrawables(null, null, visible ? percentDrawable : null, null);
    }

    /**
     * @param value not formatted amount
     * @return Formatted string of amount (#,###).
     */
    public static String getFormattedCurrency(String value) {
        try {
            NumberFormat formatter = new DecimalFormat("#,###");
            return formatter.format(Double.parseDouble(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}