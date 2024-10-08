package games.rednblack.editor.utils;

import com.kotcrab.vis.ui.widget.VisTextField;

public class InputFilters {
    public static final VisTextField.TextFieldFilter ALPHANUMERIC = new VisTextField.TextFieldFilter() {
        @Override
        public boolean acceptChar(VisTextField textField, char c) {

            if (Character.isAlphabetic(c) || Character.isJavaIdentifierPart(c))
                return true;

            return false;
        }
    };
}
