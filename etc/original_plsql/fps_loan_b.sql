CREATE OR REPLACE PACKAGE BODY fps_loan IS
  --
  g_tab_drawdown fps_t_tab_drawdown;
  g_tab_payment fps_t_tab_payment;
  --
  -- vrati DueDate
  FUNCTION get_due_date (
      id_date    IN DATE,    -- rerefencny datum v mesiaci, hociktory den mesiaca
      in_due_day IN NUMBER,  -- den splacania
      in_months  IN NUMBER DEFAULT 0)  -- 0 - DueDate tohoto mesiaca, 1 - nasledujuci DueDate, x - DueDate o x mesiacov od id_date
  RETURN DATE
  IS
    ld_due_date DATE;
  BEGIN
    IF in_due_day = 31 THEN -- posledny den v mesiaci
      RETURN last_day(add_months(id_date,in_months));
    ELSE
      ld_due_date := trunc(add_months(id_date,in_months),'MM') + in_due_day - 1;
      IF trunc(ld_due_date,'MM') = add_months(trunc(id_date,'MM'),in_months) THEN
        RETURN ld_due_date;
      ELSE
        RETURN last_day(add_months(id_date,in_months));
      END IF;
    END IF;
  END;
  --
  -- pre zadany DueDate urci datum splatnosti. plati pravidlo, ze datum splatnosti je due date, ak je nepracovny, tak je datum splatnosti najblizsi predosly pracovny
  -- den, okrem pripadu, kedy by predchadzajuci pracovny den bol v predchadzajucom mesiaci. v tom pripade je to najbliszi nasledujuci pracovny den
  FUNCTION get_maturity_date (
      id_due_date IN DATE,
      iv_country_name IN grl_countries.country_name%TYPE)
  RETURN DATE
  IS
    ld_work_date DATE;
    ld_maturity_date DATE := trunc(id_due_date,'DD');
  BEGIN
    LOOP
      ld_work_date := grl_get_nearest_working_date(ld_maturity_date, iv_country_name);
      EXIT WHEN ld_work_date = ld_maturity_date;
      ld_maturity_date := ld_maturity_date - 1;
    END LOOP;
    IF trunc(ld_maturity_date,'MM') = trunc(id_due_date,'MM') THEN
      RETURN ld_maturity_date;
    ELSE
      RETURN grl_get_nearest_working_date(id_due_date, iv_country_name);
    END IF;
  END;
  --
  -- podla periody splacania vrati pocet platieb za rok
  FUNCTION get_payments_per_anum (
      iv_payment_period IN grl_rate_periods.rate_period_code%TYPE)
  RETURN NUMBER
  IS
  BEGIN
    IF iv_payment_period = '1M' THEN
      RETURN 12;
    END IF;
  END;
  --
  -- vypocita vysku anuitnej splatky (iba podla vzorca)
  FUNCTION get_anuity (
      in_outstanding     IN NUMBER,   -- vyska uveru
      in_interest_rate   IN NUMBER,   -- urokova sadzba p.a.
      in_num_of_payments IN NUMBER,   -- pocet splatok
      iv_payment_period  IN grl_rate_periods.rate_period_code%TYPE DEFAULT '1M',  -- perioda splatok, default mesacne
      in_days_in_year    IN NUMBER DEFAULT 365,  -- pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
      in_interest_days   IN NUMBER DEFAULT 360,  -- podla bazy urocenia, u anuit je to 30denna, tj. 12*30 = 360
      in_periodic_fee    IN NUMBER DEFAULT 0,    -- periodicky poplatok
      in_rounding        IN NUMBER DEFAULT 0)    -- uroven zaokruhlovania, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
  RETURN NUMBER
  IS
    lnR1 NUMBER;
    lnR2 NUMBER;
    ln_days_in_year NUMBER := in_days_in_year;
  --
  BEGIN
    IF in_days_in_year = 366 THEN
      SELECT to_number(to_char(to_date('31.12.'||to_char(sysdate,'YYYY'),'DD.MM.YYYY'),'DDD')) INTO ln_days_in_year FROM dual;
    END  IF;
    IF iv_payment_period = '1M' THEN
      lnR1 := (in_interest_rate/(get_payments_per_anum(iv_payment_period)*100))*(in_days_in_year/in_interest_days);
      lnR2 := power(1+lnR1,in_num_of_payments);
      RETURN round( ((in_outstanding*lnR2*lnR1)/(lnR2-1)) + in_periodic_fee, in_rounding);
    ELSE
      RETURN -1;
    END IF;
  END;
  --
  -- vypocita vysku uveru podla mesacnej splatky a splatnosti
  PROCEDURE get_outstanding (
      on_outstanding     OUT NUMBER,    -- vyska uveru
      in_num_of_payments IN NUMBER,     -- pocet splatok
      pn_anuity          IN OUT NUMBER, -- mesacna splatka
      in_interest_rate   IN NUMBER,     -- urokova sadzba p.a.
      in_payment_period  IN grl_rate_periods.rate_period_code%TYPE DEFAULT '1M',  -- perioda splatok, default mesacne
      in_days_in_year    IN NUMBER DEFAULT 365,  -- pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
      in_interest_days   IN NUMBER DEFAULT 360,  -- podla bazy urocenia, u anuit je to 30denna, tj. 12*30 = 360
      in_periodic_fee    IN NUMBER DEFAULT 0,    -- periodicky poplatok
      in_rounding        IN NUMBER DEFAULT 0)    -- uroven zaokruhlovania, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
  IS
    lnR1 NUMBER;
    lnR2 NUMBER;
    ln_outstanding NUMBER;
  BEGIN
    IF in_payment_period = '1M' THEN
      pn_anuity := greatest(pn_anuity, in_periodic_fee + power(10, 0-in_rounding)); -- vyska musi byt vyssia ako pravidelny poplatok
      lnR1 := (in_interest_rate/(get_payments_per_anum(in_payment_period)*100))*(in_days_in_year/in_interest_days);
      lnR2 := power(1+lnR1,in_num_of_payments);
      ln_outstanding := round( ((pn_anuity-in_periodic_fee)*(lnR2-1))/(lnR2*lnR1) , in_rounding);
      on_outstanding := ln_outstanding;
      -- vyska splatky sa prisposobi tomu, aby posledna splatka nebola diametralne odlisna od ostatnych
      pn_anuity := get_anuity (ln_outstanding, in_interest_rate, in_num_of_payments, in_payment_period, in_days_in_year,
        in_interest_days, in_periodic_fee, in_rounding);
    ELSE
      on_outstanding :=  -1;
    END IF;
  END;
  --
  -- vypocita pocet splatok podla vysky uveru a vysky mesacnej splatky
  procedure get_num_of_payments (
      in_outstanding     IN NUMBER,     -- vyska uveru
      on_num_of_payments OUT NUMBER,    -- pocet splatok
      pn_anuity          IN OUT NUMBER, -- mesacna splatka
      in_interest_rate   IN NUMBER,     -- urokova sadzba p.a.
      in_payment_period  IN grl_rate_periods.rate_period_code%TYPE DEFAULT '1M',  -- perioda splatok, default mesacne
      in_days_in_year    IN NUMBER DEFAULT 365,  -- pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
      in_interest_days   IN NUMBER DEFAULT 360,  -- podla bazy urocenia, u anuit je to 30denna, tj. 12*30 = 360
      in_periodic_fee    IN NUMBER DEFAULT 0,    -- periodicky poplatok
      in_rounding        IN NUMBER DEFAULT 0)    -- uroven zaokruhlovania, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
  IS
    lnR1 NUMBER;
    lnCon NUMBER;
    ln_num_of_payments NUMBER;
  BEGIN
    IF in_payment_period = '1M' THEN
      lnR1 := (in_interest_rate/(get_payments_per_anum(in_payment_period)*100))*(in_days_in_year/in_interest_days);
      -- platba musi byt vyssia ako pravidelny poplatok a musi byt vyssia ako urok, inak by stav dlhu iba rastol
      pn_anuity := greatest(pn_anuity, round( (in_outstanding*lnR1) + in_periodic_fee + (0.5*power(10, 0-in_rounding)) , in_rounding) + power(10, 0-in_rounding) );
      lnCon := (pn_anuity - in_periodic_fee)/(in_outstanding*lnR1);
      ln_num_of_payments := ceil(ln(lnCon/(lnCon-1)) / ln(1+lnR1));
      on_num_of_payments := ln_num_of_payments;
      -- vyska splatky sa prisposobi tomu, aby posledna splatka nebola diametralne odlisna od ostatnych
      pn_anuity := get_anuity (in_outstanding, in_interest_rate, ln_num_of_payments, in_payment_period, in_days_in_year, in_interest_days, in_periodic_fee, in_rounding);
    ELSE
      on_num_of_payments :=  -1;
    END IF;
  END;
  --
  -- podla zadanych parametrov vygeneruje splatkovy plan
  PROCEDURE create_repayment_plan (
      id_anuity_start_date IN DATE, -- datum obdobia splacania anuity, 
      in_due_day           IN NUMBER, -- den splacania
      iv_country_name      IN grl_countries.country_name%TYPE, -- krajina, pre ktoru sa maju pocitat pracovne, nepracovne dni
      --
      in_outstanding       IN NUMBER,   -- vyska uveru
      in_interest_rate     IN NUMBER,   -- urokova sadzba p.a.
      in_num_of_payments   IN NUMBER,   -- pocet splatok
      iv_payment_period    IN grl_rate_periods.rate_period_code%TYPE DEFAULT '1M',  -- perioda splatok, default mesacne
      in_days_in_year      IN NUMBER DEFAULT 365,  -- pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
      in_interest_days     IN NUMBER DEFAULT 360,  -- podla bazy urocenia, u anuit je to 30denna, tj. 12*30 = 360
      in_periodic_fee      IN NUMBER DEFAULT 0,    -- periodicky poplatok
      in_rounding          IN NUMBER DEFAULT 0,    -- uroven zaokruhlovania, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
      --
      iv_interest_correction_type  IN VARCHAR2 DEFAULT 'CEIL', -- 'ROUND' - urok zaokruhlit, 'TRUNC' - urok orezat, 'CEIL' - zarovnat nahor,
                                                               -- 'NONE' - urok neupravovat - vyuzit celu sirku typu number v oracle
      in_interest_correction_level IN NUMBER DEFAULT 2, -- uroven zaokruhlovania/orezavania, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
      iv_last_payment_type IN VARCHAR2 DEFAULT 'CALCULATED')  -- ako vypocitat anuitu : 'CALCULATED' - iba podla vzorca, poslednu splatku nijako neupravovat,
                                                              -- 'LEAST_DIFFERENCE' tak, aby bol rozdiel medzi riadnymi splatkami a poslednou minimalny,
                                                              -- 'LOWER_THAN_REGULAR_WITH_LEAST_DIFFERENCE' - tak aby posledna splatka bola vzdy nizsia ako
                                                              -- riadna anuita, ale aby rozdiel riadnej anuity a poslednej splatky bol minimalny
  IS
    TYPE t_rec_anuity IS RECORD (
      regular_anuity NUMBER,  -- vyska riadnej anuity
      last_anuity    NUMBER); -- vyska poslednej splatky
    lr_calc_last       t_rec_anuity;
    lr_calc_actual     t_rec_anuity;
    lr_calc_new        t_rec_anuity;
    ln_regular_anuity NUMBER := get_anuity (in_outstanding, in_interest_rate, in_num_of_payments, iv_payment_period, in_days_in_year,
                                            in_interest_days, in_periodic_fee, in_rounding);
    ln_outstanding NUMBER;
    ld_due_date DATE;
    ld_interest_from DATE;
    ln_principal_sum NUMBER;
    ld_maturity_date DATE;
    ld_interest_to DATE;
    ln_interest NUMBER;
    ln_payment NUMBER;
    ln_principal NUMBER;
    ln_step NUMBER := 1;
    lb_exit BOOLEAN := FALSE;
  BEGIN
    IF iv_payment_period != '1M' THEN
      raise_application_error(-20000,'payment period not implemented');
    END IF;
--    DBMS_OUTPUT.PUT_LINE(iv_last_payment_type);
    LOOP
      IF g_tab_payment IS NOT NULL THEN
        g_tab_payment.delete;
      END IF;
      g_tab_payment := fps_t_tab_payment();
      ln_principal_sum := 0;
      ln_outstanding := in_outstanding;
      ld_interest_from := id_anuity_start_date;
      ld_due_date := get_due_date(id_anuity_start_date, in_due_day, 1);
--      DBMS_OUTPUT.PUT_LINE('Step: '||to_char(ln_step));
--      DBMS_OUTPUT.PUT_LINE('in fro int to due dt mat date       payment  interests   principal  outstanding');
--      DBMS_OUTPUT.PUT_LINE('------ ------ ------ ---------- ----------- ---------- ----------- ------------');
      FOR i in 1..in_num_of_payments LOOP
        ld_maturity_date := get_maturity_date(ld_due_date, iv_country_name);
        ld_interest_to := ld_due_date - 1;
        ln_interest := ((ld_interest_to - ld_interest_from + 1) * ln_outstanding * (in_interest_rate/100)) / in_interest_days;
        IF iv_interest_correction_type = 'ROUND' THEN
          ln_interest := round(ln_interest, in_interest_correction_level);
        ELSIF iv_interest_correction_type = 'TRUNC' THEN
          ln_interest := trunc(ln_interest, in_interest_correction_level);
        ELSIF iv_interest_correction_type = 'CEIL' THEN
          ln_interest := ceil(power(10,in_interest_correction_level)*ln_interest) / power(10,in_interest_correction_level);
        END IF;
        IF i<in_num_of_payments THEN
          ln_payment := ln_regular_anuity;
          ln_principal := ln_payment - ln_interest - in_periodic_fee;
          ln_principal_sum := ln_principal_sum + ln_principal;
        ELSE
          ln_principal := in_outstanding - ln_principal_sum;
          ln_payment := ln_principal + ln_interest + in_periodic_fee;
        END IF;
        g_tab_payment.EXTEND;
        g_tab_payment(i) := fps_t_obj_payment.Init(ld_interest_from, ld_interest_to, ld_due_date, ld_maturity_date, ln_payment, ln_interest,
          ln_principal, ln_outstanding-ln_principal);
--        DBMS_OUTPUT.PUT_LINE(to_char(ld_interest_from,'dd.mm.')||' '||to_char(ld_interest_to,'dd.mm.')||' '||
--            to_char(ld_due_date,'dd.mm.')||' '||to_char(ld_maturity_date,'dd.mm.yyyy')||' '||
--            to_char(ln_payment,'0000000.00')||' '||to_char(ln_interest,'000000.00')||' '||
--            to_char(ln_principal,'0000000.00')||' '||to_char(ln_outstanding-ln_principal,'00000000.00'));
        ld_interest_from := ld_due_date;
        IF iv_payment_period = '1M' THEN
          ld_due_date := get_due_date(ld_due_date, in_due_day, 1);
        END IF;
        ln_outstanding := ln_outstanding - ln_regular_anuity + in_periodic_fee + ln_interest;
      END LOOP;
      lr_calc_actual.regular_anuity := g_tab_payment(g_tab_payment.first).payment;
      lr_calc_actual.last_anuity := g_tab_payment(g_tab_payment.last).payment;
--      DBMS_OUTPUT.PUT_LINE('Act.ReguA='||to_char(lr_calc_actual.regular_anuity,'0000000D00')||' Act.LastA='||to_char(lr_calc_actual.last_anuity,'0000000D00')||
--                           ' Dif='||to_char(lr_calc_actual.regular_anuity-lr_calc_actual.last_anuity,'0000000D00'));
      EXIT WHEN lb_exit;
      IF (lr_calc_actual.last_anuity - lr_calc_actual.regular_anuity) > 0 THEN
        lr_calc_new.regular_anuity := lr_calc_actual.regular_anuity + power(10,0-in_rounding);
      ELSE
        lr_calc_new.regular_anuity := lr_calc_actual.regular_anuity - power(10,0-in_rounding);
      END IF;
      CASE iv_last_payment_type 
        WHEN 'CALCULATED' THEN
          EXIT;
        WHEN 'LEAST_DIFFERENCE' THEN
          IF lr_calc_last.regular_anuity = lr_calc_new.regular_anuity THEN -- podla pravidla urcena nova anuita je taka ista ako predchadzajuca => koniec
            IF abs(lr_calc_last.regular_anuity - lr_calc_last.last_anuity) < abs(lr_calc_actual.regular_anuity - lr_calc_actual.last_anuity) THEN
              lr_calc_actual := lr_calc_last;
              lb_exit := TRUE;
            ELSE
              EXIT;
            END IF;
          ELSE
            lr_calc_last := lr_calc_actual;
            lr_calc_actual := lr_calc_new;
          END IF;
        WHEN 'LOWER_THAN_REGULAR_WITH_LEAST_DIFFERENCE' THEN
          IF lr_calc_last.regular_anuity = lr_calc_new.regular_anuity THEN -- podla pravidla urcena nova anuita je taka ista ako predchadzajuca => koniec
            IF lr_calc_actual.regular_anuity - lr_calc_actual.last_anuity < 0 THEN
              lr_calc_actual := lr_calc_last;
              lb_exit := TRUE;
            ELSE
              EXIT;
            END IF;
          ELSE
            lr_calc_last := lr_calc_actual;
            lr_calc_actual := lr_calc_new;
          END IF;
        ELSE EXIT;
      END CASE;
      ln_regular_anuity := lr_calc_actual.regular_anuity;
--      DBMS_OUTPUT.PUT_LINE('New.ReguA='||to_char(lr_calc_new.regular_anuity,'0000000D00'));
      ln_step := ln_step + 1;
      EXIT WHEN ln_step > 1000; -- konci sa po prilis velkom pocte iteracii
    END LOOP;
  END;
  --
  -- podla zadanych parametrov vrati splatkovy plan v XML
  PROCEDURE get_repayment_plan (
      ox_payment_plan      OUT XMLType, -- splatkovy plan v XML,
      id_anuity_start_date IN DATE, -- datum obdobia splacania anuity, 
      in_due_day           IN NUMBER, -- den splacania
      iv_country_name      IN grl_countries.country_name%TYPE, -- krajina, pre ktoru sa maju pocitat pracovne, nepracovne dni
      in_outstanding       IN NUMBER,   -- vyska uveru
      in_interest_rate     IN NUMBER,   -- urokova sadzba p.a.
      in_num_of_payments   IN NUMBER,   -- pocet splatok
      iv_payment_period    IN grl_rate_periods.rate_period_code%TYPE DEFAULT '1M',  -- perioda splatok, default mesacne
      in_days_in_year      IN NUMBER DEFAULT 365,  -- pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
      in_interest_days     IN NUMBER DEFAULT 360,  -- podla bazy urocenia, u anuit je to 30denna, tj. 12*30 = 360
      in_periodic_fee      IN NUMBER DEFAULT 0,    -- periodicky poplatok
      in_rounding          IN NUMBER DEFAULT 0,    -- uroven zaokruhlovania, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
      iv_interest_correction_type  IN VARCHAR2 DEFAULT 'CEIL', -- 'ROUND' - urok zaokruhlit, 'TRUNC' - urok orezat, 'CEIL' - zarovnat nahor,
                                                               -- 'NONE' - urok neupravovat - vyuzit celu sirku typu number v oracle
      in_interest_correction_level IN NUMBER DEFAULT 2, -- uroven zaokruhlovania/orezavania, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
      iv_last_payment_type IN VARCHAR2 DEFAULT 'CALCULATED')   -- ako vypocitat anuitu : 'CALCULATED' - iba podla vzorca, poslednu splatku nijako neupravovat,
                                                               -- 'LEAST_DIFFERENCE' tak, aby bol rozdiel medzi riadnymi splatkami a poslednou minimalny,
                                                               -- 'LOWER_THAN_REGULAR_WITH_LEAST_DIFFERENCE' - tak aby posledna splatka bola vzdy nizsia ako 
                                                               -- riadna anuita, ale aby rozdiel riadnej anuity a poslednej splatky bol minimalny
  IS
    ll_buffer CLOB;
    lv_buffer VARCHAR(32767);
    i NUMBER;
  BEGIN
    create_repayment_plan(id_anuity_start_date, in_due_day, iv_country_name, in_outstanding, in_interest_rate, in_num_of_payments, iv_payment_period,
      in_days_in_year, in_interest_days, in_periodic_fee, in_rounding, iv_interest_correction_type, in_interest_correction_level, iv_last_payment_type);
    DBMS_LOB.CreateTemporary(ll_buffer,TRUE);
    lv_buffer := '<data>'||chr(10);
    DBMS_LOB.WriteAppend(ll_buffer, length(lv_buffer), lv_buffer);
    FOR i IN g_tab_payment.FIRST..g_tab_payment.LAST LOOP
      lv_buffer := '<repayment>'||
                   '<interest-from>'||to_char(g_tab_payment(i).interest_from,'DD.MM.YYYY')||'</interest-from>'||
                   '<interest-to>'||to_char(g_tab_payment(i).interest_to,'DD.MM.YYYY')||'</interest-to>'||
                   '<due-date>'||to_char(g_tab_payment(i).due_date,'DD.MM.YYYY')||'</due-date>'||
                   '<maturity-date>'||to_char(g_tab_payment(i).maturity_date,'DD.MM.YYYY')||'</maturity-date>'||
                   '<payment>'||to_char(g_tab_payment(i).payment,'FM9999999999999990D00')||'</payment>'||
                   '<interest>'||to_char(g_tab_payment(i).interest,'FM9999999999999990D00')||'</interest>'||
                   '<principal>'||to_char(g_tab_payment(i).principal,'FM9999999999999990D00')||'</principal>'||
                   '<outstanding>'||to_char(g_tab_payment(i).outstanding,'FM9999999999999990D00')||'</outstanding>'||
                   '</repayment>'||chr(10);
      DBMS_LOB.WriteAppend(ll_buffer, length(lv_buffer), lv_buffer);
    END LOOP;
    lv_buffer := '</data>';
    DBMS_LOB.WriteAppend(ll_buffer, length(lv_buffer), lv_buffer);
    ox_payment_plan := xmltype.createxml(ll_buffer);
    DBMS_LOB.Trim(ll_buffer,0);
  END;
  --
  -- vyhlda RPSN podla splatkoveho kalendara a planu cerpani metodou plenia intervalu, predpoklad : bude medzi 0 - 1000
  FUNCTION get_rpsn (
      it_drawdown       IN fps_t_tab_drawdown,
      it_payment        IN fps_t_tab_payment,
      id_start_drawdown IN DATE,
      in_days_in_year   IN NUMBER DEFAULT 365,  -- pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
      in_precision      IN NUMBER DEFAULT 5) -- uroven presnosti RPSN na des. miest, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
  RETURN NUMBER
  IS
    TYPE t_rec_iteration IS RECORD (
      rpsn         NUMBER,
      drawdown_sum NUMBER,
      payment_sum  NUMBER);
    lr_iter_left  t_rec_iteration;
    lr_iter_right t_rec_iteration;
    lr_iter_mid   t_rec_iteration;
    ln_steps      NUMBER := 1;
    ln_days_in_year NUMBER := in_days_in_year;
  BEGIN
    -- kontrola planov
    SELECT sum(amount) INTO lr_iter_mid.drawdown_sum FROM TABLE(CAST(it_drawdown AS fps_t_tab_drawdown));
    SELECT sum(principal) INTO lr_iter_mid.payment_sum FROM TABLE(CAST(it_payment AS fps_t_tab_payment));
    IF lr_iter_mid.drawdown_sum IS NULL OR lr_iter_mid.payment_sum IS NULL OR lr_iter_mid.drawdown_sum != lr_iter_mid.payment_sum THEN
      RETURN -1;
    END IF;
    -- vypocet RPSN polenim intervalu
    lr_iter_left.rpsn := 0;
    IF in_days_in_year = 366 THEN
      SELECT to_number(to_char(to_date('31.12.'||to_char(id_start_drawdown,'YYYY'),'DD.MM.YYYY'),'DDD')) INTO ln_days_in_year FROM dual;
    END  IF;
    SELECT sum(amount/power(1+lr_iter_left.rpsn, (drawdown_date - id_start_drawdown)/ln_days_in_year)) INTO lr_iter_left.drawdown_sum
      FROM TABLE(CAST(it_drawdown AS fps_t_tab_drawdown));
    SELECT sum(Payment/power(1+lr_iter_left.rpsn, (maturity_date - id_start_drawdown)/ln_days_in_year)) INTO lr_iter_left.payment_sum
      FROM TABLE(CAST(it_payment AS fps_t_tab_payment));
    lr_iter_right.rpsn := 1000; -- pocita sa s maximalnou vyskou RPSN. pre normalne sadzby (max desiatky percent) staci startovat od takejto max vysky RPSN
    SELECT sum(amount/power(1+lr_iter_right.rpsn, (drawdown_date - id_start_drawdown)/ln_days_in_year)) INTO lr_iter_right.drawdown_sum
      FROM TABLE(CAST(it_drawdown AS fps_t_tab_drawdown));
    SELECT sum(Payment/power(1+lr_iter_right.rpsn, (maturity_date - id_start_drawdown)/ln_days_in_year)) INTO lr_iter_right.payment_sum
      FROM TABLE(CAST(it_payment AS fps_t_tab_payment));
    LOOP
      lr_iter_mid.rpsn := (lr_iter_left.rpsn + lr_iter_right.rpsn) / 2;
      EXIT WHEN round(lr_iter_mid.rpsn, in_precision) = round(lr_iter_left.rpsn, in_precision)
           OR round(lr_iter_mid.rpsn, in_precision) = round(lr_iter_right.rpsn, in_precision);
      SELECT sum(amount/power(1+lr_iter_mid.rpsn, (drawdown_date - id_start_drawdown)/ln_days_in_year)) INTO lr_iter_mid.drawdown_sum
        FROM TABLE(CAST(it_drawdown AS fps_t_tab_drawdown));
      SELECT sum(Payment/power(1+lr_iter_mid.rpsn, (maturity_date - id_start_drawdown)/ln_days_in_year)) INTO lr_iter_mid.payment_sum
        FROM TABLE(CAST(it_payment AS fps_t_tab_payment));
--      DBMS_OUTPUT.PUT_LINE('Step : '||ln_steps);
--      DBMS_OUTPUT.PUT_LINE(to_char(lr_iter_left.rpsn,'00000000D000000')||' '||to_char(lr_iter_mid.rpsn,'00000000D000000')||' '||
--                           to_char(lr_iter_right.rpsn,'00000000D000000'));
--      DBMS_OUTPUT.PUT_LINE(to_char(lr_iter_left.drawdown_sum,'00000000D000000')||' '||to_char(lr_iter_mid.drawdown_sum,'00000000D000000')||' '||
--                           to_char(lr_iter_right.drawdown_sum,'00000000D000000'));
--      DBMS_OUTPUT.PUT_LINE(to_char(lr_iter_left.payment_sum,'00000000D000000')||' '||to_char(lr_iter_mid.payment_sum,'00000000D000000')||' '||
--                           to_char(lr_iter_right.payment_sum,'00000000D000000'));
      IF lr_iter_mid.drawdown_sum - lr_iter_mid.payment_sum > 0 AND lr_iter_left.drawdown_sum - lr_iter_left.payment_sum > 0 OR
         lr_iter_mid.drawdown_sum - lr_iter_mid.payment_sum <= 0 AND lr_iter_left.drawdown_sum - lr_iter_left.payment_sum <= 0 THEN
        lr_iter_left := lr_iter_mid;
      ELSIF lr_iter_mid.drawdown_sum - lr_iter_mid.payment_sum > 0 AND lr_iter_right.drawdown_sum - lr_iter_right.payment_sum > 0 OR
            lr_iter_mid.drawdown_sum - lr_iter_mid.payment_sum <= 0 AND lr_iter_right.drawdown_sum - lr_iter_right.payment_sum <= 0 THEN
        lr_iter_right := lr_iter_mid;
      ELSE
        RETURN -2;
      END IF;
      ln_steps := ln_steps + 1;
      IF ln_steps = 100 THEN
        RETURN -3;
      END IF;
    END LOOP;
    RETURN round(100*lr_iter_mid.rpsn, in_precision-2);
  END;
  --
  -- pre jednoduchy uver (cerpanie naraz, splacanie anuitne, mesacne) vygeneruje splatkovy plan a spocita RPSN
  PROCEDURE simulate_simple_loan(
      ox_payment_plan      OUT XMLType, -- splatkovy plan v XML,
      on_rpsn              OUT NUMBER,  -- rpsn
      id_drawdown_date     IN DATE,     -- datum cerpania, pre single uver sa pocita, ze v den cerpania sa vycerpa vsetko naraz
      in_due_day           IN NUMBER DEFAULT NULL,   -- den splacania, ak nie je vyplneny, tak je to den nasledujuci po dni cerpania
      iv_country_name      IN grl_countries.country_name%TYPE, -- krajina, pre ktoru sa maju pocitat pracovne, nepracovne dni
      in_outstanding       IN NUMBER,   -- vyska uveru
      in_interest_rate     IN NUMBER,   -- urokova sadzba p.a.
      in_num_of_payments   IN NUMBER,   -- pocet splatok
      iv_payment_period    IN grl_rate_periods.rate_period_code%TYPE DEFAULT '1M',  -- perioda splatok, default mesacne
      in_days_in_year      IN NUMBER DEFAULT 365,  -- pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
      in_interest_days     IN NUMBER DEFAULT 360,  -- podla bazy urocenia, u anuit je to 30denna, tj. 12*30 = 360
      in_periodic_fee      IN NUMBER DEFAULT 0,    -- periodicky poplatok
      in_rounding          IN NUMBER DEFAULT 0,    -- uroven zaokruhlovania, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
      iv_interest_correction_type  IN VARCHAR2 DEFAULT 'CEIL', -- 'ROUND' - urok zaokruhlit, 'TRUNC' - urok orezat, 'CEIL' - zarovnat nahor,
                                                               -- 'NONE' - urok neupravovat - vyuzit celu sirku typu number v oracle
      in_interest_correction_level IN NUMBER DEFAULT 2, -- uroven zaokruhlovania/orezavania, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
      iv_last_payment_type IN VARCHAR2 DEFAULT 'CALCULATED',  -- ako vypocitat anuitu : 'CALCULATED' - iba podla vzorca, poslednu splatku nijako neupravovat,
                                                              -- 'LEAST_DIFFERENCE' tak, aby bol rozdiel medzi riadnymi splatkami a poslednou minimalny,
                                                              -- 'LOWER_THAN_REGULAR_WITH_LEAST_DIFFERENCE' - tak aby posledna splatka bola vzdy nizsia ako
                                                              -- riadna anuita, ale aby rozdiel riadnej anuity a poslednej splatky bol minimalny
      in_precision         IN NUMBER DEFAULT 5)  -- uroven presnosti RPSN na des. miest, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
  IS
    ln_due_day NUMBER := NVL(in_due_day, to_number(to_char(id_drawdown_date+1,'DD')));
    lt_drawdown fps_t_tab_drawdown;
    lt_payment fps_t_tab_payment;
  BEGIN
    get_repayment_plan (ox_payment_plan, id_drawdown_date+1, ln_due_day, iv_country_name, in_outstanding, in_interest_rate, in_num_of_payments, iv_payment_period, 
      in_days_in_year, in_interest_days, in_periodic_fee, in_rounding, iv_interest_correction_type, in_interest_correction_level, iv_last_payment_type);
    lt_drawdown := fps_t_tab_drawdown();
    lt_drawdown.EXTEND;
    lt_drawdown(1) := fps_t_obj_drawdown.Init(id_drawdown_date, in_outstanding);
    lt_payment := fps_t_tab_payment();
    lt_payment := g_tab_payment;
    on_rpsn :=   get_rpsn (lt_drawdown, lt_payment, id_drawdown_date, in_days_in_year, in_precision);
  END;
  --
/*  --
  -- pre jednoduchy uver (cerpanie naraz, splacanie anuitne, mesacne) vygeneruje splatkovy plan a spocita RPSN
  PROCEDURE simulate_simple_loan(
      ov_payment_plan      OUT VARCHAR2, -- splatkovy plan v VARCHAR2,
      on_rpsn              OUT NUMBER,  -- rpsn
      id_drawdown_date     IN DATE,     -- datum cerpania, pre single uver sa pocita, ze v den cerpania sa vycerpa vsetko naraz
      in_due_day           IN NUMBER DEFAULT NULL,   -- den splacania, ak nie je vyplneny, tak je to den nasledujuci po dni cerpania
      iv_country_name      IN grl_countries.country_name%TYPE, -- krajina, pre ktoru sa maju pocitat pracovne, nepracovne dni
      in_outstanding       IN NUMBER,   -- vyska uveru
      in_interest_rate     IN NUMBER,   -- urokova sadzba p.a.
      in_num_of_payments   IN NUMBER,   -- pocet splatok
      iv_payment_period    IN grl_rate_periods.rate_period_code%TYPE DEFAULT '1M',  -- perioda splatok, default mesacne
      in_days_in_year      IN NUMBER DEFAULT 365,  -- pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
      in_interest_days     IN NUMBER DEFAULT 360,  -- podla bazy urocenia, u anuit je to 30denna, tj. 12*30 = 360
      in_periodic_fee      IN NUMBER DEFAULT 0,    -- periodicky poplatok
      in_rounding          IN NUMBER DEFAULT 0,    -- uroven zaokruhlovania, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
      iv_interest_correction_type  IN VARCHAR2 DEFAULT 'CEIL', -- 'ROUND' - urok zaokruhlit, 'TRUNC' - urok orezat, 'CEIL' - zarovnat nahor,
                                                               -- 'NONE' - urok neupravovat - vyuzit celu sirku typu number v oracle
      in_interest_correction_level IN NUMBER DEFAULT 2, -- uroven zaokruhlovania/orezavania, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
      iv_last_payment_type IN VARCHAR2 DEFAULT 'CALCULATED',  -- ako vypocitat anuitu : 'CALCULATED' - iba podla vzorca, poslednu splatku nijako neupravovat,
                                                              -- 'LEAST_DIFFERENCE' tak, aby bol rozdiel medzi riadnymi splatkami a poslednou minimalny,
                                                              -- 'LOWER_THAN_REGULAR_WITH_LEAST_DIFFERENCE' - tak aby posledna splatka bola vzdy nizsia ako
                                                              -- riadna anuita, ale aby rozdiel riadnej anuity a poslednej splatky bol minimalny
      in_precision         IN NUMBER DEFAULT 5)  -- uroven presnosti RPSN na des. miest, 0 - cele cisla, 1 - jedno des. miesto, -1 cele desiatky
  IS
    lx_payment_plan XMLType;
  BEGIN
    fps_loan.simulate_simple_loan(lx_payment_plan, on_rpsn, id_drawdown_date, in_due_day, iv_country_name, in_outstanding, in_interest_rate, in_num_of_payments,
      iv_payment_period, in_days_in_year, in_interest_days, in_periodic_fee, in_rounding, iv_interest_correction_type, in_interest_correction_level,
      iv_last_payment_type, in_precision);
    ov_payment_plan := lx_payment_plan.getstringval();
  END;
  --*/
END;
/