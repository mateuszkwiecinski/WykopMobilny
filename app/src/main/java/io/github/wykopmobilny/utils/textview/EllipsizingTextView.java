package io.github.wykopmobilny.utils.textview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.regex.Pattern;

import io.github.wykopmobilny.R;

/**
 * A {@link android.widget.TextView} that ellipsizes more intelligently.
 * This class supports ellipsizing multiline text through setting {@code android:ellipsize}
 * and {@code android:maxLines}.
 * <p/>
 * Note: {@link android.text.TextUtils.TruncateAt#MARQUEE} ellipsizing type is not supported.
 */
public class EllipsizingTextView extends AppCompatTextView {
    public static final int MAX_LINES = 8;
    private static final SpannableString ELLIPSIS = new SpannableString("\u0020[pokaż\u00A0całość]");

    private static final Pattern DEFAULT_END_PUNCTUATION = Pattern.compile("[.!?,;:…]*$", Pattern.DOTALL);
    private EllipsizeStrategy ellipsizeStrategy;
    private boolean isEllipsized;
    private boolean isStale;
    private boolean programmaticChange;
    private CharSequence fullText;
    private int maxLines;
    private float lineSpacingMult = 1.0f;
    private float lineAddVertPad = 0.0f;

    /**
     * The end punctuation which will be removed when appending {@link #ELLIPSIS}.
     */
    private final Pattern endPunctPattern = DEFAULT_END_PUNCTUATION;

    public EllipsizingTextView(Context context) {
        this(context, null);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
            new int[]{android.R.attr.maxLines, android.R.attr.ellipsize}, defStyle, 0);
        setMaxLines(a.getInt(0, Integer.MAX_VALUE));
        a.recycle();
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        @ColorInt int color = typedValue.data;
        ELLIPSIS.setSpan(new ForegroundColorSpan(color), 0, ELLIPSIS.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public boolean isEllipsized() {
        return isEllipsized;
    }

    /**
     * @return The maximum number of lines displayed in this {@link android.widget.TextView}.
     */
    public int getMaxLines() {
        return maxLines;
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        this.maxLines = maxLines;
        isStale = true;
    }

    /**
     * Determines if the last fully visible line is being ellipsized.
     *
     * @return {@code true} if the last fully visible line is being ellipsized;
     * otherwise, returns {@code false}.
     */
    public boolean ellipsizingLastFullyVisibleLine() {
        return maxLines == Integer.MAX_VALUE;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        lineAddVertPad = add;
        lineSpacingMult = mult;
        super.setLineSpacing(add, mult);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!programmaticChange) {
            fullText = text instanceof Spanned ? (Spanned) text : text;
            isStale = true;
        }
        super.setText(text, type);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (ellipsizingLastFullyVisibleLine()) {
            isStale = true;
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        if (ellipsizingLastFullyVisibleLine()) {
            isStale = true;
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (isStale) {
            resetText();
        }
        super.onDraw(canvas);
    }

    /**
     * Sets the ellipsized text if appropriate.
     */
    private void resetText() {
        int maxLines = getMaxLines();
        CharSequence workingText = fullText;
        boolean ellipsized = false;

        if (maxLines != -1) {
            if (ellipsizeStrategy == null) {
                setEllipsize(null);
            }
            workingText = ellipsizeStrategy.processText(fullText);
            ellipsized = !ellipsizeStrategy.isInLayout(fullText);
        }

        if (!workingText.equals(getText())) {
            programmaticChange = true;
            try {
                setText(workingText);
            } finally {
                programmaticChange = false;
            }
        }

        isStale = false;
        if (ellipsized != isEllipsized) {
            isEllipsized = ellipsized;
        }
    }

    /**
     * Causes words in the text that are longer than the view is wide to be ellipsized
     * instead of broken in the middle. Use {@code null} to turn off ellipsizing.
     * <p/>
     * Note: Method does nothing for {@link android.text.TextUtils.TruncateAt#MARQUEE}
     * ellipsizing type.
     *
     * @param where part of text to ellipsize
     */
    @Override
    public void setEllipsize(TruncateAt where) {
        if (where == null) {
            ellipsizeStrategy = new EllipsizeNoneStrategy();
            return;
        }

        switch (where) {
            case END:
                ellipsizeStrategy = new EllipsizeEndStrategy();
                break;
            case START:
                ellipsizeStrategy = new EllipsizeStartStrategy();
                break;
            case MIDDLE:
                ellipsizeStrategy = new EllipsizeMiddleStrategy();
                break;
            case MARQUEE:
            default:
                ellipsizeStrategy = new EllipsizeNoneStrategy();
                break;
        }
    }

    /**
     * A listener that notifies when the ellipsize state has changed.
     */
    public interface EllipsizeListener {
        void ellipsizeStateChanged(boolean ellipsized);
    }

    /**
     * A base class for an ellipsize strategy.
     */
    private abstract class EllipsizeStrategy {
        /**
         * Returns ellipsized text if the text does not fit inside of the layout;
         * otherwise, returns the full text.
         *
         * @param text text to process
         * @return Ellipsized text if the text does not fit inside of the layout;
         * otherwise, returns the full text.
         */
        public CharSequence processText(CharSequence text) {
            return !isInLayout(text) ? createEllipsizedText(text) : text;
        }

        /**
         * Determines if the text fits inside of the layout.
         *
         * @param text text to fit
         * @return {@code true} if the text fits inside of the layout;
         * otherwise, returns {@code false}.
         */
        public boolean isInLayout(CharSequence text) {
            Layout layout = createWorkingLayout(text);
            return layout.getLineCount() <= getLinesCount();
        }

        /**
         * Creates a working layout with the given text.
         *
         * @param workingText text to create layout with
         * @return {@link android.text.Layout} with the given text.
         */
        protected Layout createWorkingLayout(CharSequence workingText) {
            return new StaticLayout(workingText, getPaint(),
                getWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight(),
                Alignment.ALIGN_NORMAL, lineSpacingMult,
                lineAddVertPad, false /* includepad */);
        }

        /**
         * Get how many lines of text we are allowed to display.
         */
        protected int getLinesCount() {
            if (ellipsizingLastFullyVisibleLine()) {
                int fullyVisibleLinesCount = getFullyVisibleLinesCount();
                return fullyVisibleLinesCount == -1 ? 1 : fullyVisibleLinesCount;
            } else {
                return maxLines;
            }
        }

        /**
         * Get how many lines of text we can display so their full height is visible.
         */
        protected int getFullyVisibleLinesCount() {
            Layout layout = createWorkingLayout("");
            int height = getHeight() - getCompoundPaddingTop() - getCompoundPaddingBottom();
            int lineHeight = layout.getLineBottom(0);
            return height / lineHeight;
        }

        /**
         * Creates ellipsized text from the given text.
         *
         * @param fullText text to ellipsize
         * @return Ellipsized text
         */
        protected abstract CharSequence createEllipsizedText(CharSequence fullText);
    }

    /**
     * An {@link EllipsizingTextView.EllipsizeStrategy} that
     * does not ellipsize text.
     */
    private class EllipsizeNoneStrategy extends EllipsizeStrategy {
        @Override
        protected CharSequence createEllipsizedText(CharSequence fullText) {
            return fullText;
        }
    }

    /**
     * An {@link EllipsizingTextView.EllipsizeStrategy} that
     * ellipsizes text at the end.
     */
    private class EllipsizeEndStrategy extends EllipsizeStrategy {
        @Override
        protected CharSequence createEllipsizedText(CharSequence fullText) {
            Layout layout = createWorkingLayout(fullText);
            int cutOffIndex = layout.getLineEnd(maxLines - 1);
            int textLength = fullText.length();
            int cutOffLength = textLength - cutOffIndex;
            if (cutOffLength < ELLIPSIS.length()) {
                cutOffLength = ELLIPSIS.length();
            }
            CharSequence workingText = TextUtils.substring(fullText, 0, textLength - cutOffLength).trim();

            while (!isInLayout(TextUtils.concat(stripEndPunctuation(workingText), ELLIPSIS))) {
                int lastSpace = TextUtils.lastIndexOf(workingText, ' ');
                if (lastSpace == -1) {
                    break;
                }
                workingText = TextUtils.substring(workingText, 0, lastSpace).trim();
            }

            workingText = TextUtils.concat(stripEndPunctuation(workingText), ELLIPSIS);
            SpannableStringBuilder dest = new SpannableStringBuilder(workingText);

            if (fullText instanceof Spanned) {
                TextUtils.copySpansFrom((Spanned) fullText, 0, workingText.length() - 15, null, dest, 0);
            }
            return dest;
        }

        /**
         * Strips the end punctuation from a given text according to {@link #endPunctPattern}.
         *
         * @param workingText text to strip end punctuation from
         * @return Text without end punctuation.
         */
        public String stripEndPunctuation(CharSequence workingText) {
            return endPunctPattern.matcher(workingText).replaceFirst("");
        }
    }

    /**
     * An {@link EllipsizingTextView.EllipsizeStrategy} that
     * ellipsizes text at the start.
     */
    private class EllipsizeStartStrategy extends EllipsizeStrategy {
        @Override
        protected CharSequence createEllipsizedText(CharSequence fullText) {
            Layout layout = createWorkingLayout(fullText);
            int cutOffIndex = layout.getLineEnd(maxLines - 1);
            int textLength = fullText.length();
            int cutOffLength = textLength - cutOffIndex;
            if (cutOffLength < ELLIPSIS.length()) {
                cutOffLength = ELLIPSIS.length();
            }
            CharSequence workingText = TextUtils.substring(fullText, cutOffLength, textLength).trim();

            while (!isInLayout(TextUtils.concat(ELLIPSIS, workingText))) {
                int firstSpace = TextUtils.indexOf(workingText, ' ');
                if (firstSpace == -1) {
                    break;
                }
                workingText = TextUtils.substring(workingText, firstSpace, workingText.length()).trim();
            }

            workingText = TextUtils.concat(ELLIPSIS, workingText);
            SpannableStringBuilder dest = new SpannableStringBuilder(workingText);

            if (fullText instanceof Spanned) {
                TextUtils.copySpansFrom((Spanned) fullText, textLength - workingText.length(), textLength, null, dest, 0);
            }
            return dest;
        }
    }

    /**
     * An {@link EllipsizingTextView.EllipsizeStrategy} that
     * ellipsizes text in the middle.
     */
    private class EllipsizeMiddleStrategy extends EllipsizeStrategy {
        @Override
        protected CharSequence createEllipsizedText(CharSequence fullText) {
            Layout layout = createWorkingLayout(fullText);
            int cutOffIndex = layout.getLineEnd(maxLines - 1);
            int textLength = fullText.length();
            int cutOffLength = textLength - cutOffIndex;
            if (cutOffLength < ELLIPSIS.length()) {
                cutOffLength = ELLIPSIS.length();
            }
            cutOffLength += cutOffIndex % 2;    // Make it even.
            String firstPart = TextUtils.substring(fullText, 0, textLength / 2 - cutOffLength / 2).trim();
            String secondPart = TextUtils.substring(fullText, textLength / 2 + cutOffLength / 2, textLength).trim();

            while (!isInLayout(TextUtils.concat(firstPart, ELLIPSIS, secondPart))) {
                int lastSpaceFirstPart = firstPart.lastIndexOf(' ');
                int firstSpaceSecondPart = secondPart.indexOf(' ');
                if (lastSpaceFirstPart == -1 || firstSpaceSecondPart == -1) {
                    break;
                }
                firstPart = firstPart.substring(0, lastSpaceFirstPart).trim();
                secondPart = secondPart.substring(firstSpaceSecondPart).trim();
            }

            SpannableStringBuilder firstDest = new SpannableStringBuilder(firstPart);
            SpannableStringBuilder secondDest = new SpannableStringBuilder(secondPart);

            if (fullText instanceof Spanned) {
                TextUtils.copySpansFrom((Spanned) fullText, 0, firstPart.length(), null, firstDest, 0);
                TextUtils.copySpansFrom((Spanned) fullText, textLength - secondPart.length(), textLength, null, secondDest, 0);
            }
            return TextUtils.concat(firstDest, ELLIPSIS, secondDest);
        }
    }
}
