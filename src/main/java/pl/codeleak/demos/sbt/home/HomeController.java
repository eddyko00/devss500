package pl.codeleak.demos.sbt.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;

@Controller
class HomeController {

    @GetMapping("/")
    String index(Model model) {

        return "index";
    }
    @GetMapping("/index.html")
    String indexindex(Model model) {

        return "index";
    }

    @GetMapping("/web")
    String index1(Model model) {

        return "index_web";
    }
    @GetMapping("/account")
    String account(Model model) {
        return "account";
    }
    @GetMapping("/account_1")
    String account_1(Model model) {
        return "account_1";
    }
    @GetMapping("/accountstatus")
    String accountstatus(Model model) {
        return "accountstatus";
    }
    @GetMapping("/accountstatus_1")
    String accountstatus_1(Model model) {
         return "accountstatus_1";
    }
    @GetMapping("/monanalyize")
    String monanalyize(Model model) {
        return "monanalyize";
    }
    @GetMapping("/monmonitor")
    String monmonitor(Model model) {
        return "monmonitor";
    }
    @GetMapping("/monmonitor_1")
    String monmonitor_1(Model model) {
        return "monmonitor_1";
    }
    @GetMapping("/monmonitor_2")
    String monmonitor_2(Model model) {
        return "monmonitor_2";
    }
    @GetMapping("/monmonitortc")
    String monmonitortc(Model model) {
         return "monmonitortc";
    }
    @GetMapping("/monserv")
    String monserv(Model model) {
        return "monserv";
    }
    @GetMapping("/monserv_1")
    String monserv_1(Model model) {
         return "monserv_1";
    }
    @GetMapping("/monservfeat")
    String monservfeat(Model model) {
         return "monservfeat";
    }
    @GetMapping("/monservfeat_1")
    String monservfeat_1(Model model) {
        return "monservfeat_1";
    }
    @GetMapping("/monservfeattest")
    String monservfeattest(Model model) {
        return "monservfeattest";
    }
    @GetMapping("/monservfeattestres")
    String monservfeattestres(Model model) {
         return "monservfeattestres";
    }
    @GetMapping("/monservfeattestres_1")
    String monservfeattestres_1(Model model) {
         return "monservfeattestres_1";
    }
    @GetMapping("/reganalyize")
    String reganalyize(Model model) {
         return "reganalyize";
    }
    @GetMapping("/regmonitor")
    String regmonitor(Model model) {
         return "regmonitor";
    }
    @GetMapping("/regmonitor_1")
    String regmonitor_1(Model model) {
         return "regmonitor_1";
    }
    @GetMapping("/regmonitor_2")
    String regmonitor_2(Model model) {
         return "regmonitor_2";
    }
    @GetMapping("/regmonitortc")
    String regmonitortc(Model model) {
         return "regmonitortc";
    }
    @GetMapping("/splunkanalyize")
    String splunkanalyize(Model model) {
         return "splunkanalyize";
    }
    @GetMapping("/splunkserv")
    String splunkserv(Model model) {
         return "splunkserv";
    }
    @GetMapping("/splunkserv_1")
    String splunkserv_1(Model model) {
         return "splunkserv_1";
    }
    @GetMapping("/splunkservfeat")
    String splunkservfeat(Model model) {
         return "splunkservfeat";
    }
    @GetMapping("/splunkservfeat_1")
    String splunkservfeat_1(Model model) {
         return "splunkservfeat_1";
    }
//////////////////////////////////////
    @GetMapping("properties")
    @ResponseBody
    java.util.Properties properties() {
        return System.getProperties();
    }

}
