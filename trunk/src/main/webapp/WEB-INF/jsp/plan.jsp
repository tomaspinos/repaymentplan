<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="content-language" content="cs"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <meta http-equiv="Expires" content="-1"/>
    <meta name="keywords" content="kalkulačka,splátkový kalendář,úvěr,úrok,RPSN,splátky,p.a.,navýšení,úroková sazba"/>
    <meta name="description"
          content="Úvěrová kalkulačka (simulace úvěru) - generování splátkového kalendáře včetně výpočtu RPSN podle zadaných podmínek (výše úvěru, úrok, délka úvěru)."/>
    <meta name="robots" content="index, follow"/>
    <meta name="author" content="Aiteq Ltd."/>
    <title>(TEST) Úvěrová kalkulačka: výpočet splátkového kalendáře a RPSN.</title>
    <link rel="stylesheet" type="text/css" href="css/default.css"/>
    <script type="text/javascript"
            src="http://w.sharethis.com/button/sharethis.js#publisher=f93045e8-d282-42ce-add4-ab05aecb2d84&amp;type=website&amp;post_services=facebook%2Ctwitter%2Clinkedin%2Cdelicious%2Cblogger%2Cwordpress%2Cmyspace%2Cdigg%2Cwindows_live%2Cgoogle_bmarks%2Cemail%2Csms%2Cstumbleupon%2Creddit%2Cbebo%2Cybuzz%2Cyahoo_bmarks%2Cmixx%2Ctechnorati%2Cfriendfeed%2Cpropeller%2Cnewsvine%2Cxanga&amp;button=false"></script>
</head>
<body>
<div class="centered">
    <h1>(TEST) Úvěrová kalkulačka <span>(výpočet splátkového kalendáře a RPSN)</span></h1>

    <p>Spočítejte si <strong>měsíční splátku</strong> úvěru se zobrazením její struktury (úrok a splátka jistiny).
        Naše <strong>úvěrová kalkulačka</strong> vám po zadání vstupních parametrů vygeneruje
        <strong>splátkový kalendář</strong> a spočítá <strong><abbr
                title="Roční procentní sazba nákladů">RPSN</abbr></strong>.
        Kalkulačka je aplikovatelná na všechny typy úvěrů. Narozdíl od ostatních kalkulátorů volně dostupných na
        internetu
        lze zvolit způsob výpočtu anuity (úprava poslední splátky).
        Výsledný splátkový kalendář je též generován s ohledem na pracovní dny a státní svátky.</p>

    <div class="share">
        <a id="ck_facebook" class="stbar chicklet" href="javascript:void(0);"><img
                src="http://w.sharethis.com/chicklets/facebook.gif"/></a>
        <a id="ck_twitter" class="stbar chicklet" href="javascript:void(0);"><img
                src="http://w.sharethis.com/chicklets/twitter.gif"/></a>
        <a id="ck_sharethis" class="stbar chicklet" href="javascript:void(0);"><img
                src="http://w.sharethis.com/chicklets/sharethis.gif"/>&laquo; sdílet</a>
        <script type="text/javascript">
            var shared_object = SHARETHIS.addEntry({
                        title: document.title,
                        url: document.location.href
                    });
            shared_object.attachButton(document.getElementById("ck_sharethis"));
            shared_object.attachChicklet("facebook", document.getElementById("ck_facebook"));
            shared_object.attachChicklet("twitter", document.getElementById("ck_twitter"));
        </script>
    </div>

    <form:form commandName="form">

        <div>
            <form:errors path="*"/>
        </div>

        <table class="form" cellspacing="0" cellpadding="0" border="0">
            <tr>
                <td class="left">
                    <form:label path="outstanding">Výše úvěru:</form:label>
                    <form:input path="outstanding"/>
                </td>
                <td>
                    <form:label path="dropdownDate">Datum čerpání:</form:label>
                    <form:input path="dropdownDate"/>
                </td>
            </tr>
            <tr>
                <td class="left">
                    <form:label path="interestRate">Úroková sazba p.a.:</form:label>
                    <form:input path="interestRate"/>
                    <div class="info">výše úroku v procentech z nominální částkyročně</div>
                </td>
                <td>
                    <form:label path="dueDay">Den splácení:</form:label>
                    <form:input path="dueDay"/>
                    <div class="info">den v měsíci (1 - 31)</div>
                </td>
            </tr>
            <tr>
                <td class="left">
                    <form:label path="payments">Počet splátek:</form:label>
                    <form:input path="payments"/>
                </td>
                <td>
                    <form:label path="country">Země:</form:label>
                    <form:select path="country" items="${countries}" itemValue="code" itemLabel="label"/>
                    <div class="info">ovlivní datumy splátek v generovaném kalendáři</div>
                </td>
            </tr>
            <tr>
                <td class="left">
                    <form:label path="fee">Periodický poplatek:</form:label>
                    <form:input path="fee"/>
                </td>
                <td>
                    <form:label path="lastPaymentType">Poslední splátka:</form:label>
                    <form:select path="lastPaymentType" items="${lastPaymentTypes}" itemValue="code" itemLabel="label"/>
                </td>
            </tr>
            <tr>
                <td colspan="2" class="button">
                    <input type="submit" value="Generovat splátkový kalendář"/>
                </td>
            </tr>
        </table>

    </form:form>

    <c:if test="${form.loanSimulation != null}">

    <p class="rpsn">RPSN: ${form.loanSimulation.rpsn}%</p>
    <table class="plan" cellspacing="0" cellpadding="0" border="0">
        <thead>
        <tr>
            <th class="left">#</th>
            <th>Počátek úročení</th>
            <th>Konec úročení</th>
            <th>Předepsaný<br/>datum splátky</th>
            <th>Skutečný<br/>datum splátky</th>
            <th>Výška splátky</th>
            <th>Úrok</th>
            <th>Úmor</th>
            <th>Zůstatek</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="paymentIndex" value="${1}"/>
        <c:forEach items="${form.loanSimulation.payments}" var="payment">
            <tr>
                <td class="left">${paymentIndex}</td>
                <td><fmt:formatDate value="${payment.interestFromAsDate}" pattern="dd.MM.yyyy"/></td>
                <td><fmt:formatDate value="${payment.interestToAsDate}" pattern="dd.MM.yyyy"/></td>
                <td><fmt:formatDate value="${payment.dueDateAsDate}" pattern="dd.MM.yyyy"/></td>
                <td><fmt:formatDate value="${payment.maturityDateAsDate}" pattern="dd.MM.yyyy"/></td>
                <td class="number"><fmt:formatNumber value="${payment.payment}" minFractionDigits="2" maxFractionDigits="2"/></td>
                <td class="number"><fmt:formatNumber value="${payment.interest}" minFractionDigits="2" maxFractionDigits="2"/></td>
                <td class="number"><fmt:formatNumber value="${payment.principal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                <td class="number"><fmt:formatNumber value="${payment.outstanding}" minFractionDigits="2" maxFractionDigits="2"/></td>
            </tr>
            <c:set var="paymentIndex" value="${paymentIndex + 1}"/>
        </c:forEach>
        </tbody>
    </table>

    </c:if>

    <h2>Slovník pojmů</h2>
    <table class="dict" cellspacing="0" cellpadding="0" border="0">
        <tr>
            <td><dfn>Anuita</dfn></td>
            <td>Pravidelná splátka úvěru, která zahrnuje úrok a úmor. Její výše se v čase nemění, ale mění se její
                struktura (poměr úroku a úmoru).
            </td>
        </tr>
        <tr>
            <td><dfn>Jistina</dfn></td>
            <td>Jistina úvěru je objem prostředků, které si dlužník půjčil. Z této částky se počítá úrok.</td>
        </tr>
        <tr>
            <td><dfn>Koeficient navýšení</dfn></td>
            <td>Procentuální vyjádření poměru mezi celkovými náklady na úvěr (jistina, úrok, poplatky) a výší půjčky.
                Narozdíl od úrokové sazby nezohledňuje časovou osu.
            </td>
        </tr>
        <tr>
            <td><dfn>Navýšení</dfn></td>
            <td>Rozdíl mezi výší úvěru a součtem splátek.</td>
        </tr>
        <tr>
            <td><dfn>p.a.</dfn></td>
            <td>Roční úroková sazba (z latinského <em>per annum</em>).</td>
        </tr>
        <tr>
            <td><dfn>p.m.</dfn></td>
            <td>Měsíční úroková sazba (z latinského <em>per mensem</em>).</td>
        </tr>
        <tr>
            <td><dfn>RPSN</dfn></td>
            <td>Roční procentní sazba nákladů. V procentech udává podíl z dlužné částky, kterou musí dlužník zaplatit za
                jeden rok v souvislosti se splátkami (veškeré výdaje spojenými s čerpáním úvěru). Více na <a
                        href="http://cs.wikipedia.org/wiki/RPSN">Wikipedii</a></td>
        </tr>
        <tr>
            <td><dfn>Úroková sazba</dfn></td>
            <td>Procentní poměr úroku k jistině za dané časové období.</td>
        </tr>
        <tr>
            <td><dfn>Úmor</dfn></td>
            <td>Pravidelná splátka jistiny.</td>
        </tr>
    </table>
    <table class="links" cellspacing="0" cellpadding="0" border="0">
        <tr>
            <td>
                <a href="http://www.aiteq.com" title="Aiteq Ltd. - way to e-business">Aiteq Ltd.</a>&nbsp;|
                <a href="http://www.deponest.com" title="DEPONEST - Software Escrow Agent">Úschova zdrojových kódů
                    (software escrow)</a>&nbsp;|
                <a href="http://www.software-escrow.cz" title="Blog o úschově zdrojových kódů (software escrow)">Blog o
                    software escrow</a>&nbsp;|
                <a href="http://www.aiteq.com/services/body-shopping" title="Pronájem IT specialistů (bodyshopping)">Pronájem
                    IT specialistů</a><br/>
                <a href="http://yau.sh" title="Yet Another URL Shortener">URL Shortener</a>
            </td>
            <td align="right">copyright (c) 2010 <a href="http://www.aiteq.com" title="Aiteq Ltd. - way to e-business">Aiteq
                Ltd.</a></td>
        </tr>
    </table>
    <table class="backlinks" cellspacing="0" cellpadding="0" border="0">
        <tr>
            <td>
                <a href="http://linkuj.cz/?id=linkuj&url=http://www.splatkovy-kalendar.cz&title=Úvěrová kalkulačka: výpočet splátkového kalendáře a RPSN."
                   title="Přidat úvěrovou kalkulačku na linkuj.cz">Přidat na linkuj.cz</a>&nbsp;|
                <a href="http://www.em-design.cz/shop/pages-links/" title="Katalog odkazů">.: EM-LINKS :.</a>&nbsp;|
                <a href="http://www.em-design.cz/shop/pages-pagerank/" title="Pagerank">.: MiniRank :.</a>&nbsp;|
                <a title="katalog, český katalog, katalog stránek, katalog firem, obchodů, iLinker.cz" target="_blank"
                   href="http://www.ilinker.cz/">Český katalog</a>&nbsp;|
                <a title="inkatalog, inkatalog.cz, katalog stránek, český katalog stránek, katalog obchodů, firem, eshopů"
                   target="_blank" href="http://www.inkatalog.cz/">Český katalog stránek</a>&nbsp;|
                <a href="http://www.katalog-seo.eu">SEO katalog</a>&nbsp;|
                <a href="http://toplink.miliweb.net" title="Toplink - katalog odkazů">Toplink - katalog odkazů</a>&nbsp;|
                Zobrazit <a href="http://www.superlink.cz/" title="TopList SuperLink.cz"
                            onclick="window.location='http://www.superlink.cz/odkazy/Penize'; return false;">Peníze</a>&nbsp;|
                <a href="http://www.sprehledem.cz" target="_blank">sPřehledem.cz</a>&nbsp;|
                <a href="http://topsites.cz" title="Top List">TopSites.cz</a>&nbsp;|
                <a href="http://1x.cz">Katalog 1x.cz</a>&nbsp;|
                <a href="http://www.jagg.cz/bookmarks.php?action=add&address=http://www.splatkovy-kalendar.cz&title=Úvěrová%20kalkulačka%20(splátkový%20kalendář)">Jaggni
                    to!</a>
                <a class="inv" href="http://www.toplist.cz/" target="_top"><img
                        src="http://toplist.cz/count.asp?id=1069298" alt="TOPlist" border="0"></a>
            </td>
        </tr>
    </table>
</div>
<script src="http://www.aiteq.com/static/ga.js" type="text/javascript"></script>
<script type="text/javascript">
    try {
        var pageTracker = _gat._getTracker("UA-876013-6");
    } catch(err) {
    }
</script>
<script src="http://www.aiteq.com/static/gax.js" type="text/javascript"></script>
</body>
</html>
