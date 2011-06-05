package cz.repaymentplan.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

/**
 * @author Tomas Pinos
 */
public class FixedLocaleResolver extends AbstractLocaleResolver {

    private static final Locale LOCALE_CS = new Locale("cs", "CZE");

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return LOCALE_CS;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
    }
}
