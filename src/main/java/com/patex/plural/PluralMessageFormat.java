package com.patex.plural;

import java.text.MessageFormat;

class PluralMessageFormat {


    private final PluralChooser chooser;

    PluralMessageFormat(PluralChooser chooser) {
        this.chooser = chooser;
    }

    String format(String source, Object... args) {
        return format(source.toCharArray(), args);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    private String format(char[] source, Object... args) {
        StringBuilder result = new StringBuilder(source.length);
        int argIndex = -1;
        //find nearest left argument index
        for (int i = 0; i < source.length; i++) {
            if (source[i] == '{') {
                int indexBegin = i + 1;
                result.append(source[i++]);
                int indexEnd = -1;
                for (; i < source.length && source[i] != '}'; i++) {
                    if (source[i] == ',') {
                        indexEnd = i;
                    }
                    result.append(source[i]);
                }
                if (i == source.length) {
                    throw malformed(source, indexBegin - 1, "unclosed '{' - missing '}'");
                }
                if (indexEnd == -1) {
                    indexEnd = i;
                }
                argIndex = parseIndex(source, indexBegin, indexEnd);
                result.append(source[i]);
                //find plural format
            } else if (source[i] == '<' &&
                    source.length > i + 3 && source[i + 1] == 'p' && source[i + 2] == ':'){
                int tagBegin = i;
                i += 3;
                //if index argument set directly
                if (source[i] == '{') {
                    int indexBegin = i + 1;
                    for (; i < source.length && source[i] != '}'; i++) {
                    }
                    if (i == source.length) {
                        throw malformed(source, tagBegin, "unclosed '<p:{' - missing '}'");
                    }
                    argIndex = parseIndex(source, indexBegin, i);
                    i++;
                }
                int wordBegin = i;
                for (; i < source.length && source[i] != '>'; i++) {
                }
                if (i == source.length) {
                    throw malformed(source, tagBegin, "unclosed '<p:' tag - missing '>'");
                }
                int end = i;

                String word = String.copyValueOf(source, wordBegin, end - wordBegin);
                if (argIndex < 0 || argIndex >= args.length) {
                    throw malformed(source, tagBegin, "plural tag '<p:" + word + ">' refers to argument index "
                            + argIndex + " but only " + args.length + " argument(s) were supplied"
                            + (argIndex == -1 ? " (no preceding '{n}' or explicit '{n}' index found)" : ""));
                }
                if (!(args[argIndex] instanceof Integer)) {
                    Object arg = args[argIndex];
                    throw malformed(source, tagBegin, "plural tag '<p:" + word + ">' needs an Integer argument at index "
                            + argIndex + " but got " + (arg == null ? "null" : arg.getClass().getName()));
                }
                String form = chooser.getForm(word, (Integer) args[argIndex]);
                result.append(form);
            } else{
                result.append(source[i]);
            }
        }
        return new MessageFormat(result.toString(), chooser.getLocale()).format(args);
    }

    private static int parseIndex(char[] source, int begin, int end) {
        try {
            return Integer.parseInt(String.valueOf(source, begin, end - begin));
        } catch (NumberFormatException e) {
            throw malformed(source, begin, "expected a numeric argument index but found '"
                    + String.valueOf(source, begin, end - begin) + "'");
        }
    }

    private static PluralFormatException malformed(char[] source, int position, String reason) {
        return new PluralFormatException("Malformed plural pattern at position " + position + ": " + reason
                + ". Pattern: \"" + String.valueOf(source) + "\"");
    }

}