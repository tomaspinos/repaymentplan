package cz.repaymentplan.web;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cz.repaymentplan.logic.LoanSimulation;
import cz.repaymentplan.logic.LoanSimulationAlgorithm;
import cz.repaymentplan.logic.enums.Country;
import cz.repaymentplan.logic.enums.InterestCorrectionType;
import cz.repaymentplan.logic.enums.LastPaymentType;
import cz.repaymentplan.logic.enums.PaymentPeriod;

/**
 * @author Tomas Pinos
 */
@Controller
public class ApplicationController {

    @Autowired
    private LoanSimulationAlgorithm loanSimulationAlgorithm;

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(value = "/showPlan.do", method = RequestMethod.GET)
    public String showPlan() {
        return "plan";
    }

    @RequestMapping(value = "/showPlan.do", method = RequestMethod.POST)
    public String calculatePlan(@Valid @ModelAttribute("form") PlanForm form, BindingResult result) {
        if (result.hasErrors()) {
            for (ObjectError error : result.getAllErrors()) {
                System.out.println("error = " + error);
            }

            return "plan";
        }

        LoanSimulation loanSimulation = loanSimulationAlgorithm.simulateSimpleLoan(
                new DateTime(form.getDropdownDate()), form.getDueDay(), Country.valueOf(form.getCountry()), form.getOutstanding(), form.getInterestRate(), form.getPayments(),
                PaymentPeriod.ONE_MONTH, 365, 360, form.getFee(), InterestCorrectionType.CEIL, LastPaymentType.valueOf(form.getLastPaymentType()));

        form.setLoanSimulation(loanSimulation);

        return "plan";
    }

    @ModelAttribute("form")
    public PlanForm getForm() {
        PlanForm form = new PlanForm();

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        form.setDropdownDate(tomorrow.getTime());

        Calendar tomorrowTomorrow = Calendar.getInstance();
        tomorrowTomorrow.add(Calendar.DATE, 2);
        form.setDueDay(tomorrowTomorrow.get(Calendar.DAY_OF_MONTH));

        return form;
    }

    @ModelAttribute("countries")
    public List<Country> getCountries() {
        return Arrays.asList(Country.values());
    }

    @ModelAttribute("lastPaymentTypes")
    public List<LastPaymentType> getLastPaymentTypes() {
        return Arrays.asList(LastPaymentType.values());
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));

        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("cs", "CZE"));
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        binder.registerCustomEditor(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, numberFormat, true));
    }
}
