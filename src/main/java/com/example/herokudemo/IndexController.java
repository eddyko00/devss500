package com.example.herokudemo;

import com.afweb.model.*;

import com.afweb.util.*;
import com.afweb.service.ServiceAFweb;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

//https://www.baeldung.com/spring-cors
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class IndexController {

    private static AFwebService afWebService = new AFwebService();

    @GetMapping("/")
    public String index() {
        return "Hello there! I'm running v1.1";
    }
//////////////////////////////////////////////

    @RequestMapping(value = "/ping", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    WebStatus serverPing() {
        WebStatus msg = new WebStatus();

        msg = afWebService.serverPing();
        return msg;
    }

    //////////////
    @RequestMapping(value = "/cust/{username}/sys/resetdb", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    WebStatus SystemResetDB(@PathVariable("username") String username) {
        WebStatus msg = new WebStatus();
        // remote is stopped
        if (CKey.UI_ONLY == true) {
            return null;
        }
        if (ServiceAFweb.getServerObj().isSysMaintenance() == true) {
            if (username.toLowerCase().equals(CKey.ADMIN_USERNAME.toLowerCase())) {
                msg.setResponse(afWebService.SystemRestDBData());
                msg.setResult(true);
                return msg;
            }
        }
        CustomerObj cust = afWebService.getCustomerIgnoreMaintenance(username, null);
        if (cust != null) {
            if (cust.getType() == CustomerObj.INT_ADMIN_USER) {
                msg.setResponse(afWebService.SystemRestDBData());
                msg.setResult(true);
                return msg;
            }
        }
        return null;
    }

    @RequestMapping(value = "/cust/{username}/sys/stop", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    WebStatus SystemStop(@PathVariable("username") String username) {
        WebStatus msg = new WebStatus();

        if (username.toLowerCase().equals(CKey.ADMIN_USERNAME.toLowerCase())) {
            msg.setResponse(afWebService.SystemStop());
            msg.setResult(true);
            return msg;
        }

        return null;
    }

    @RequestMapping(value = "/cust/{username}/sys/start", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    WebStatus SystemStart(@PathVariable("username") String username) {
        WebStatus msg = new WebStatus();
        if (ServiceAFweb.getServerObj().isSysMaintenance() == true) {
            if (username.toLowerCase().equals(CKey.ADMIN_USERNAME.toLowerCase())) {
                msg.setResponse(afWebService.SystemStart());
                msg.setResult(true);
                return msg;
            }
        }

        CustomerObj cust = afWebService.getCustomerIgnoreMaintenance(username, null);
        if (cust != null) {
            if (cust.getType() == CustomerObj.INT_ADMIN_USER) {
                msg.setResponse(afWebService.SystemStart());
                msg.setResult(true);
                return msg;
            }
        }
        return null;
    }
    @RequestMapping(value = "/cust/{username}/sys/backupall", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    WebStatus SystemBackupacc(@PathVariable("username") String username) {
        WebStatus msg = new WebStatus();
        // remote is stopped
        CustomerObj cust = afWebService.getCustomerIgnoreMaintenance(username, null);
        if (cust != null) {
            if (cust.getType() == CustomerObj.INT_ADMIN_USER) {
                msg.setResponse(afWebService.systemBackupAll() + "");
                msg.setResult(true);
                return msg;
            }
        }

        return null;
    }



    @RequestMapping(value = "/cust/{username}/sys/reopenssnsdata", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    WebStatus SystemReopen(@PathVariable("username") String username) {
        WebStatus msg = new WebStatus();
        // remote is stopped
        if (ServiceAFweb.getServerObj().isSysMaintenance() == true) {
            if (username.toLowerCase().equals(CKey.ADMIN_USERNAME.toLowerCase())) {
                msg.setResponse(afWebService.SystemClearLock());
                msg.setResult(true);
                return msg;
            }
        }
        CustomerObj cust = afWebService.getCustomerIgnoreMaintenance(username, null);
        if (cust != null) {
            if (cust.getType() == CustomerObj.INT_ADMIN_USER) {
                msg.setResponse(afWebService.SystemReOpenData());
                msg.setResult(true);
                return msg;
            }
        }

        return null;
    }

    @RequestMapping(value = "/cust/{username}/sys/clearlock", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    WebStatus SystemClearLock(@PathVariable("username") String username) {
        WebStatus msg = new WebStatus();
        // remote is stopped
        if (ServiceAFweb.getServerObj().isSysMaintenance() == true) {
            if (username.toLowerCase().equals(CKey.ADMIN_USERNAME.toLowerCase())) {
                msg.setResponse(afWebService.SystemClearLock());
                msg.setResult(true);
                return msg;
            }
        }
        CustomerObj cust = afWebService.getCustomerIgnoreMaintenance(username, null);
        if (cust != null) {
            if (cust.getType() == CustomerObj.INT_ADMIN_USER) {
                msg.setResponse(afWebService.SystemClearLock());
                msg.setResult(true);
                return msg;
            }
        }

        return null;
    }

    @RequestMapping(value = "/cust/{username}/sys/lock", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    ArrayList getLockAll(
            @PathVariable("username") String username
    ) {
        ServiceAFweb.getServerObj().setCntControRequest(ServiceAFweb.getServerObj().getCntControRequest() + 1);

        if (ServiceAFweb.getServerObj().isSysMaintenance() == true) {
            if (username.toLowerCase().equals(CKey.ADMIN_USERNAME.toLowerCase())) {
                ArrayList result = afWebService.getAllLock();
                return result;
            }
        }

        CustomerObj cust = afWebService.getCustomerPassword(username, null);
        if (cust != null) {
            if (cust.getType() == CustomerObj.INT_ADMIN_USER) {
                ArrayList result = afWebService.getAllLock();
                ServiceAFweb.getServerObj().setCntControlResp(ServiceAFweb.getServerObj().getCntControlResp() + 1);
                return result;
            }
        }
        return null;
    }


    @RequestMapping(value = "/cust/{username}/sys/request", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    RequestObj SystemSQLRequest(
            @PathVariable("username") String username,
            @RequestBody String input
    ) {
        ServiceAFweb.getServerObj().setCntControRequest(ServiceAFweb.getServerObj().getCntControRequest() + 1);
        RequestObj sqlReq = null;
        try {
            sqlReq = new ObjectMapper().readValue(input, RequestObj.class);
        } catch (IOException ex) {
            return null;
        }

        if (ServiceAFweb.getServerObj().isSysMaintenance() == true) {
            if (username.toLowerCase().equals(CKey.ADMIN_USERNAME.toLowerCase())) {
                RequestObj sqlResp = afWebService.SystemSQLRequest(sqlReq);
                return sqlResp;
            }
        }

        CustomerObj cust = afWebService.getCustomerPassword(username, null);
        if (cust != null) {
            if (cust.getType() == CustomerObj.INT_ADMIN_USER) {
                RequestObj sqlResp = afWebService.SystemSQLRequest(sqlReq);
                ServiceAFweb.getServerObj().setCntControlResp(ServiceAFweb.getServerObj().getCntControlResp() + 1);
                return sqlResp;
            }
        }
        return null;
    }

//////////////
    @RequestMapping(value = "/cust/{username}/sys/custlist", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    ArrayList getCustList(
            @PathVariable("username") String username,
            @RequestParam(value = "length", required = false) String lengthSt) {
        ServiceAFweb.getServerObj().setCntControRequest(ServiceAFweb.getServerObj().getCntControRequest() + 1);
        int length = 0; //20;
        if (lengthSt != null) {
            length = Integer.parseInt(lengthSt);
        }
        CustomerObj cust = afWebService.getCustomerPassword(username, null);
        if (cust != null) {
            if (cust.getType() == CustomerObj.INT_ADMIN_USER) {
                ArrayList custNameList = afWebService.getCustomerList(length);
                ServiceAFweb.getServerObj().setCntControlResp(ServiceAFweb.getServerObj().getCntControlResp() + 1);
                return custNameList;
            }
        }
        return null;
    }

    @RequestMapping(value = "/cust/{username}/sys/cust/{customername}/status/{status}/substatus/{substatus}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    int updateCustomer(
            @PathVariable("username") String username,
            @PathVariable("customername") String customername,
            @PathVariable("status") String status,
            @PathVariable("substatus") String substatus
    ) {

        ServiceAFweb.getServerObj().setCntControRequest(ServiceAFweb.getServerObj().getCntControRequest() + 1);
        if (customername == null) {
            return 0;
        }
        CustomerObj cust = afWebService.getCustomerPassword(username, null);
        if (cust != null) {
            if (cust.getType() == CustomerObj.INT_ADMIN_USER) {
                int result = afWebService.updateCustStatusSubStatus(customername, status, substatus);
                ServiceAFweb.getServerObj().setCntControlResp(ServiceAFweb.getServerObj().getCntControlResp() + 1);
                return result;
            }
        }
        return 0;
    }

    /////////////////////////////////////////////////////////////////////////    
    @RequestMapping(value = "/timer")
    public ModelAndView timerPage() {
        ModelAndView model = new ModelAndView("helloWorld");

        model.addObject("message", AFwebService.getServerObj().getServerName() + " " + AFwebService.getServerObj().getVerString() + "</br>"
                + AFwebService.getServerObj().getLastServUpdateESTdate() + "</br>"
                + AFwebService.getServerObj().getTimerMsg() + "</br>" + AFwebService.getServerObj().getTimerThreadMsg());
        return model;
    }
///////////////////////

    @RequestMapping(value = "/cust/{username}/sys/mysql", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    String getmysql(
            @PathVariable("username") String username,
            @RequestBody String input
    ) {
        ServiceAFweb.getServerObj().setCntControRequest(ServiceAFweb.getServerObj().getCntControRequest() + 1);
        RequestObj sqlObj = new RequestObj();
        try {
            sqlObj = new ObjectMapper().readValue(input, RequestObj.class);
        } catch (IOException ex) {
            return "";
        }
        CustomerObj cust = afWebService.getCustomerIgnoreMaintenance(username, null);
        if (cust != null) {
            if (cust.getType() == CustomerObj.INT_ADMIN_USER) {
//                System.out.println(sqlObj.getReq());
                if (sqlObj.getCmd().equals("1")) {
                    return afWebService.SystemRemoteGetMySQL(sqlObj.getReq());
                } else if (sqlObj.getCmd().equals("2")) {
                    return afWebService.SystemRemoteUpdateMySQL(sqlObj.getReq());
                } else if (sqlObj.getCmd().equals("3")) {
                    return afWebService.SystemRemoteUpdateMySQLList(sqlObj.getReq());
                }
            }
        }
        return "";
    }

    @RequestMapping(value = "/server/restoressnsacc", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    int getServerResourceAccObj() {

        return afWebService.systemRestoresSsnsAcc();
    }

    @RequestMapping(value = "/server", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    ArrayList getServerObj() {

        return afWebService.getServerList();
    }

    @RequestMapping(value = "/server/mysqldb", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    String getServerLocalDbURL() {
        return ServiceAFweb.URL_LOCAL_DB;
    }

    @RequestMapping(value = "/server/mysqldb/set", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    String setServerLocalDbURL(
            @RequestParam(value = "url", required = true) String urlSt,
            HttpServletRequest request, HttpServletResponse response
    ) {

        ServiceAFweb.URL_LOCAL_DB = urlSt.trim();
        //restart ServiceAFweb
        afWebService.SystemStart();
        return "done...";
    }

    @RequestMapping(value = "/server/filepath", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    String getServerFileP() {
        return ServiceAFweb.FileLocalPath;
    }

    @RequestMapping(value = "/server/sysfilepath", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    String getServerFileDir() {
        String userDirectory = Paths.get("").toAbsolutePath().toString();
        return userDirectory;
    }

    @RequestMapping(value = "/server/filepath/set", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    String setServerfileP(
            @RequestParam(value = "path", required = true) String pathSt,
            HttpServletRequest request, HttpServletResponse response
    ) {

        ServiceAFweb.FileLocalPath = pathSt.trim();
        return "done...";
    }

    @RequestMapping(value = "/server/url0", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    String getServerURL() {
        String url0 = RESTtimer.serverURL_0;
        if (url0.length() == 0) {
            url0 = ServiceAFweb.SERVERDB_URL;
        }
        return url0;
    }

    @RequestMapping(value = "/server/url0/set", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    String setServerURL(
            @RequestParam(value = "url", required = true) String urlSt,
            HttpServletRequest request, HttpServletResponse response
    ) {

        RESTtimer.serverURL_0 = urlSt.trim();
        return "done...";
    }

    @RequestMapping(value = "/timerhandler", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    WebStatus timerHandlerREST(
            @RequestParam(value = "resttimerMsg", required = false) String resttimerMsg
    ) {

        WebStatus msg = new WebStatus();
        msg.setResult(true);
        msg.setResultID(ConstantKey.ENABLE);

        //process timer handler
        int timerCnt = afWebService.timerHandler(resttimerMsg);

        msg.setResponse("timerCnt " + timerCnt);
        return msg;
    }

    @RequestMapping(value = "/cust/login", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    LoginObj getCustObjLogin(
            @RequestParam(value = "email", required = true) String emailSt,
            @RequestParam(value = "pass", required = true) String passSt,
            HttpServletRequest request, HttpServletResponse response
    ) {
        ServiceAFweb.getServerObj().setCntControRequest(ServiceAFweb.getServerObj().getCntControRequest() + 1);
        if (ServiceAFweb.getServerObj().isSysMaintenance() == true) {
            LoginObj loginObj = new LoginObj();
            loginObj.setCustObj(null);
            WebStatus webStatus = new WebStatus();
            webStatus.setResultID(100);
            loginObj.setWebMsg(webStatus);
            return loginObj;
        }
        if (emailSt == null) {
            return null;
        }
        if (passSt == null) {
            return null;
        }
        LoginObj loginObj = afWebService.getCustomerEmailLogin(emailSt, passSt);
        ServiceAFweb.getServerObj().setCntControlResp(ServiceAFweb.getServerObj().getCntControlResp() + 1);
        return loginObj;
    }

    @RequestMapping(value = "/cust/{username}/login", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    LoginObj getCustObjUserLogin(
            @PathVariable("username") String username,
            @RequestParam(value = "pass", required = true) String passSt,
            HttpServletRequest request, HttpServletResponse response
    ) {
        ServiceAFweb.getServerObj().setCntControRequest(ServiceAFweb.getServerObj().getCntControRequest() + 1);
        if (ServiceAFweb.getServerObj().isSysMaintenance() == true) {
            LoginObj loginObj = new LoginObj();
            loginObj.setCustObj(null);
            WebStatus webStatus = new WebStatus();
            webStatus.setResultID(100);
            loginObj.setWebMsg(webStatus);
            return loginObj;
        }
        if (passSt == null) {
            return null;
        }
        LoginObj loginObj = afWebService.getCustomerLogin(username, passSt);
        ServiceAFweb.getServerObj().setCntControlResp(ServiceAFweb.getServerObj().getCntControlResp() + 1);
        return loginObj;
    }

}
