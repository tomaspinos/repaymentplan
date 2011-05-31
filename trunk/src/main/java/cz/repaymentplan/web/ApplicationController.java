package cz.repaymentplan.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Tomas Pinos
 */
@Controller
public class ApplicationController {

    @RequestMapping("/showPlan.do")
    public String showPlan() {
        return "plan";
    }
}
