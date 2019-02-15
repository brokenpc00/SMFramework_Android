package com.interpark.smframework.util;

import android.graphics.Paint;
import android.graphics.Rect;

// texture util... 글자를 그리기 위해서 문자 단위 자름..

public class TextTextureUtil {
    private static final int MAX_LINES = 256;

    private static int starts[] = new int[MAX_LINES];
    private static int stops[] = new int[MAX_LINES];
    private static Rect bounds = new Rect();

    // those members are stored per instance to minimize
    // the number of allocations to avoid triggering the
    // GC too much

    /**
     * Calculate height of text block and prepare to draw it.
     *
     * @param text - text to draw
     * @param width - maximum width in pixels
     * @param height - maximum height in pixels
     * @returns height of text in pixels
     */
    public static String getDivideString(Paint paint, final String text, final int maxWidth, int maxLines, int[] lineCount) {
        maxLines = Math.min(Math.max(0, maxLines), MAX_LINES);

        Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
        int lines = 0;
        int textHeight = 0;
        boolean wasCut = false;

        // get maximum number of characters in one line
        paint.getTextBounds(
                "i",
                0,
                1,
                bounds );

        final int maximumInLine = maxWidth / bounds.width();
        final int length = text.length();

        if( length > 0 )
        {
            final int lineHeight = -metrics.ascent + metrics.descent;
            int start = 0;
            int stop = maximumInLine > length ? length : maximumInLine;

            for( ;; )
            {
                // skip LF and spaces
                for( ; start < length; ++start )
                {
                    char ch = text.charAt( start );

                    if( ch != '\n' &&
                            ch != '\r' &&
                            ch != '\t' &&
                            ch != ' ' )
                        break;
                }

                for( int o = stop + 1; stop < o && stop > start; )
                {
                    o = stop;

                    int lowest = text.indexOf( "\n", start );

                    paint.getTextBounds(
                            text,
                            start,
                            stop,
                            bounds );

                    if( (lowest >= start && lowest < stop) ||
                            bounds.width() > maxWidth )
                    {
                        --stop;

                        if( lowest < start ||
                                lowest > stop )
                        {
                            final int blank = text.lastIndexOf( " ", stop );
                            final int hyphen = text.lastIndexOf( "-", stop );

                            if( blank > start &&
                                    (hyphen < start || blank > hyphen) )
                                lowest = blank;
                            else if( hyphen > start )
                                lowest = hyphen;
                        }

                        if( lowest >= start &&
                                lowest <= stop )
                        {
                            final char ch = text.charAt( stop );

                            if( ch != '\n' &&
                                    ch != ' ' )
                                ++lowest;

                            stop = lowest;
                        }

                        continue;
                    }

                    break;
                }

                if( start >= stop )
                    break;

                int minus = 0;

                // cut off lf or space
                if( stop < length )
                {
                    final char ch = text.charAt( stop - 1 );

                    if( ch == '\n' ||
                            ch == ' ' )
                        minus = 1;
                }

//				if( textHeight + lineHeight > maxHeight )
//				{
//					wasCut = true;
//					break;
//				}
//
                starts[lines] = start;
                stops[lines] = stop - minus;

                if( ++lines > maxLines )
                {
                    wasCut = true;
                    break;
                }

                if( textHeight > 0 )
                    textHeight += metrics.leading;

                textHeight += lineHeight;

                if( stop >= length )
                    break;

                start = stop;
                stop = length;
            }
        }

        /// 자르기 시작
        String result = "";

        --lines;
        for( int n = 0; n <= lines; ++n )
        {
            String t;

            if (result.length() > 0) {
                if (!result.endsWith("\n")) {
                    result += "\n";
                }
            }

            if( wasCut && n == lines-1 && stops[n] - starts[n] > 3 ) {
                t = text.substring( starts[n], stops[n] - 3 ).concat( "..." );
                result += t;
                break;
            }
            else {
                t = text.substring( starts[n], stops[n] );
            }
            result += t;
        }

        if (lineCount != null) {
            lineCount[0] = lines+1;
        }

        return result;
    }
}
