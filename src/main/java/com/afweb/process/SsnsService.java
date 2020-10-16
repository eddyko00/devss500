/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.afweb.process;

import com.afweb.model.*;
import com.afweb.model.ssns.*;
import com.afweb.service.ServiceAFweb;

import com.afweb.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.Proxy;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import static org.apache.http.protocol.HTTP.USER_AGENT;

/**
 *
 * @author koed
 */
public class SsnsService {

    protected static Logger logger = Logger.getLogger("SsnsService");
    public static Set<String> set = new HashSet<>();

    public static String APP_WIFI = "wifi";
    public static String APP_APP = "app";
    public static String APP_PRODUCT = "prod";
    public static String APP_TTVC = "ttv";
    public static String APP_WLNPRO = "wlnpro";
    public static String APP_QUAL = "qual";
    public static String APP_CALLC = "call";
    public static String APP_ACTCFG = "actcfg";

    public static String APP_TTVSUB = "ttvsub";  // ETL name
    public static String APP_TTVREQ = "ttvreq";  // ETL name
    //
    public static String APP_FEAT_TYPE_WLNP = "WLNP";
    public static String APP_GET_DOWNURL = "downloadurl";

    public static String APP_FEAT_TYPE_TTV = "TTV";
    public static String APP_FEAT_TYPE_HSIC = "HSIC";
    public static String APP_FEATT_TYPE_SING = "SING";
    public static String PROD_GET_PROD = "getProductList";
    public static String PROD_GET_BYID = "getProductById";
    public static String PROD_GET_CC = "CallControl";
    public static String APP_FEATT_TYPE_CC = "CC";

    public static String APP_FEAT_TYPE_APP = "APP";
    public static String APP_GET_APP = "getAppointment";
    public static String APP_CAN_APP = "cancelAppointment";
    public static String APP_GET_TIMES = "searchTimeSlot";
    public static String APP_UPDATE = "updateAppointment";

    public static String APP_FEAT_TYPE_WIFI = "WIFI";
    public static String WI_GetDeviceStatus = "getDeviceStatus";
    public static String WI_Callback = "callbackNotification";
    public static String WI_GetDevice = "getDevices";
    public static String WI_GetDeviceHDML = "getDeviceshdml";
    public static String WI_config = "configureDeviceStatus";

    public static String APP_FEAT_TYPE_QUAL = "QUAL";
    public static String QUAL_AVAL = "availability";
    public static String QUAL_MATCH = "address_matches";

    //call control
    public static String CALLC_GET = "getCallControl";
    public static String CALLC_UPDATE = "updateCallControl";
    public static String CALLC_RESET = "resetCallFeature";

    //ActCfg activation and configuration
    public static String APP_FEAT_TYPE_ACTCFG = "ACTCFG";
    public static String ACTCFG_GET_SRV = "getService";
    public static String ACTCFG_UPDATE_SRV = "updateService";
//
    public static String APP_FEAT_TYPE_TTVCL = "TTVCL";
    public static String TT_GetSub = "getCustomerTvSubscription";
    public static String TT_Vadulate = "validateWithAuth";
    public static String TT_Quote = "quotewithauth";
    public static String TT_SaveOrder = "saveOrder";

    private SsnsDataImp ssnsDataImp = new SsnsDataImp();

////////////////////////////////////////////
    public String getFeatureSsnsQual(SsnsData dataObj) {
        String feat = "";
        try {
            feat = getFeatureSsnsQualProcess(dataObj);
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsQual Exception " + ex.getMessage());
        }
        getSsnsDataImp().updatSsnsDataStatusById(dataObj.getId(), ConstantKey.COMPLETED);
        return feat;
    }

    public String getFeatureSsnsQualProcess(SsnsData dataObj) {
        ProductData pData = new ProductData();
        ArrayList<String> cmd = new ArrayList();
        if (dataObj == null) {
            return "";
        }

        String address = "";
        int vpopReq = 0;

        String dataSt = "";
        try {
            String oper = dataObj.getOper();
            if (oper.equals(QUAL_AVAL)) { //"updateAppointment")) {

                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 3) {
                    address = operList[1];
                    if (address.length() == 0) {
                        address = operList[2];
                    }
                    if (dataSt.indexOf("1509") != -1) {
                        vpopReq = 0;
                    } else {
                        vpopReq = 1;
                    }
                }
                cmd.add("get availability"); // description
                cmd.add(QUAL_AVAL);   // cmd
                pData.setCmd(cmd);
            } else if (oper.equals(QUAL_MATCH)) {
                // cannot support this address format
                return "";
            } else {
                logger.info("> getFeatureSsnsQualProcess Other oper " + oper);
                return "";
            }

            if (address.equals("")) {
                return "";
            } else if (address.equals("null")) {
                return "";
            }

//            logger.info(dataSt);
/////////////
            //call devop to get customer id
            SsnsAcc NAccObj = new SsnsAcc();
            NAccObj.setDown("splunkflow");
            boolean stat = this.updateSsnsQual(oper, address, pData, dataObj, NAccObj);
            if (stat == true) {
                boolean exist = false;
                String key = NAccObj.getName()
                        + NAccObj.getCusid()
                        + NAccObj.getBanid()
                        + NAccObj.getTiid();
                key = key.replaceAll(NAccObj.getUid(), "");
                ArrayList<SsnsAcc> ssnsAccObjList = getSsnsDataImp().getSsnsAccObjList(NAccObj.getName(), NAccObj.getUid());
                if (set.add(key)) {
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() != 0) {
                            SsnsAcc ssnsObj = ssnsAccObjList.get(0);
                            if (ssnsObj.getDown().equals("splunkflow")) {
                                exist = true;
                            }
                        }
                    }
                }

                if (exist == false) {

                    ssnsAccObjList = getSsnsDataImp().getSsnsAccObjListByTiid(NAccObj.getName(), NAccObj.getTiid());
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() > 3) {
                            exist = true;
                        }
                    }
                }
                if (exist == false) {
                    int ret = getSsnsDataImp().insertSsnsAccObject(NAccObj);
                }
            }
            return NAccObj.getName();
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsQualProcess Exception " + ex.getMessage());
        }

        return "";
    }

    public boolean updateSsnsQual(String oper, String address, ProductData pData, SsnsData dataObj, SsnsAcc NAccObj) {
        try {
            String featTTV = "";

            if (oper.equals(QUAL_AVAL) || oper.equals(QUAL_MATCH)) {

                String outputSt = null;
                if (oper.equals(QUAL_AVAL)) {
                    outputSt = SendSsnsQual(ServiceAFweb.URL_PRODUCT_PR, oper, address, null);
                    if (outputSt == null) {
                        return false;
                    }
                }
                if (outputSt == null) {
                    return false;
                }
                if (outputSt.length() == 0) {
                    return false;
                }
//                    if (outputSt.length() < 80) {  // or test 
//                        return false;
//                    }
                if (outputSt.indexOf("responseCode:400500") != -1) {
                    return false;
                }
                featTTV = parseQualFeature(outputSt, oper);
                if (featTTV == null) {
                    return false;
                }
            } else {
                return false;
            }

//            logger.info("> updateSsnsWifi feat " + featTTV);
/////////////TTV   
            if (NAccObj.getDown().equals("splunkflow")) {
                ArrayList<String> flow = new ArrayList();
                int faulure = getSsnsFlowTrace(dataObj, flow);
                if (flow == null) {
                    logger.info("> updateSsnsQual skip no flow");
                    return false;
                }
                pData.setFlow(flow);

                if (faulure == 1) {
                    featTTV += ":splunkfailed";
                }
            }
            logger.info("> updateSsnsQual feat " + featTTV);

            NAccObj.setName(featTTV);

            NAccObj.setTiid(address);

            NAccObj.setUid(dataObj.getUid());
            NAccObj.setApp(dataObj.getApp());
            NAccObj.setOper(oper);

//          NAccObj.setDown(""); // set by NAccObj
            NAccObj.setRet(dataObj.getRet());
            NAccObj.setExec(dataObj.getExec());

            String nameSt = new ObjectMapper().writeValueAsString(pData);
            NAccObj.setData(nameSt);

            NAccObj.setUpdatedatel(dataObj.getUpdatedatel());
            NAccObj.setUpdatedatedisplay(new java.sql.Date(dataObj.getUpdatedatel()));

            return true;
        } catch (Exception ex) {
            logger.info("> updateSsnsQual Exception " + ex.getMessage());
        }
        return false;
    }

    public static String parseQualFeature(String outputSt, String oper) {

        if (outputSt == null) {
            return null;
        }

        int serviceCategoryCd = 0;
        int qualStatusCdQUA = 0;
        int qualStatusCdDQN = 0;
        String FSAStatusCd = "";
        String dropPlacedInd = "";

        ArrayList<String> outputList = ServiceAFweb.prettyPrintJSON(outputSt);
        for (int j = 0; j < outputList.size(); j++) {
            String inLine = outputList.get(j);
//            logger.info("" + inLine);

            if (inLine.indexOf("serviceCategoryCd") != -1) {
                serviceCategoryCd++;
                continue;
            }
            if (inLine.indexOf("qualStatusCd") != -1) {
                if (inLine.indexOf("QUA") != -1) {
                    qualStatusCdQUA++;
                }
                if (inLine.indexOf("DQN") != -1) {
                    qualStatusCdDQN++;
                }
                continue;
            }
            if (inLine.indexOf("FSAStatusCd") != -1) {

                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("FSAStatusCd:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                FSAStatusCd = valueSt;
                continue;
            }
            if (inLine.indexOf("dropPlacedInd") != -1) {
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("dropPlacedInd:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                dropPlacedInd = valueSt;
                continue;
            }
        }
        if (serviceCategoryCd == 0) {
            logger.info("> serviceCategoryCd=0 for multiple address found");
            return null;
        }
        String featTTV = APP_FEAT_TYPE_QUAL;
        featTTV += ":" + oper;
        featTTV += ":serviceCat_" + serviceCategoryCd;
        featTTV += ":QUA_" + qualStatusCdQUA;
        featTTV += ":DQN_" + qualStatusCdDQN;
        featTTV += ":FSASt_" + FSAStatusCd;
        featTTV += ":drop_" + dropPlacedInd;
        return featTTV;
    }

    public String SendSsnsQual(String ProductURL, String oper, String address, ArrayList<String> inList) {
        logger.info("> SendSsnsQual " + oper + " " + address);

        String url = "";

        if (oper.equals(QUAL_AVAL)) {
            address = ServiceAFweb.replaceAll(" ", "%20", address);
            url = ProductURL + "/v1/cmo/selfmgmt/service-qualification/availability?address=" + address;

        } else {
            return "";
        }
        try {
            if (inList != null) {
                inList.add(url);
            }
            // calculate elapsed time in milli seconds
            long startTime = TimeConvertion.currentTimeMillis();

            String output = this.sendRequest_Ssns(METHOD_GET, url, null, null, null);

            long endTime = TimeConvertion.currentTimeMillis();
            long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
            if (inList != null) {
                String tzid = "America/New_York"; //EDT
                TimeZone tz = TimeZone.getTimeZone(tzid);
                Date d = new Date(startTime);
                // timezone symbol (z) included in the format pattern 
                DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                // format date in target timezone
                format.setTimeZone(tz);
                String ESTdate = format.format(d);

                inList.add(ESTdate + " elapsedTime:" + elapsedTime);
                inList.add("output:");
            }

            return output;
        } catch (Exception ex) {
            logger.info("> SendSsnsQual exception " + ex.getMessage());
        }
        return null;
    }

    public String TestFeatureSsnsProdQual(SsnsAcc dataObj, ArrayList<String> outputList, String Oper, String LABURL) {
        if (dataObj == null) {
            return "";
        }
        if (LABURL.length() == 0) {
            LABURL = ServiceAFweb.URL_PRODUCT_PR;
        }
        dataObj.getData();

        String address = dataObj.getTiid();
        if (address.length() == 0) {
            return "";
        }

        String outputSt = null;

        ArrayList<String> inList = new ArrayList();
        if (Oper.equals(QUAL_AVAL)) {
            outputSt = SendSsnsQual(LABURL, Oper, address, inList);
            if (outputSt == null) {
                return "";
            }
            ////special char #, need to ignore for this system
            outputSt = outputSt.replaceAll("#", "");
            outputSt = outputSt.replaceAll("~", "");
            outputSt = outputSt.replaceAll("^", "");

            ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);
            String feat = parseQualFeature(outputSt, Oper);
            if (outputSt.indexOf("responseCode:400500") != -1) {
                feat += ":testfailed";
            }

            outputList.add(feat);
            outputList.addAll(inList);
            outputList.addAll(outList);

            return feat;
        }

        return "";
    }

    ////////////////////////////////////////////
    public String getFeatureSsnsCallC(SsnsData dataObj) {
        String feat = "";
        try {
            feat = getFeatureSsnsCallCProcess(dataObj);
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsCallC Exception " + ex.getMessage());
        }
        getSsnsDataImp().updatSsnsDataStatusById(dataObj.getId(), ConstantKey.COMPLETED);
        return feat;
    }

    public String getFeatureSsnsCallCProcess(SsnsData dataObj) {
        ProductData pData = new ProductData();
        ArrayList<String> cmd = new ArrayList();
        if (dataObj == null) {
            return "";
        }

        String appTId = "";
        String banid = "";
        String phone = "";
        String host = "";
        String dataSt = "";
        String postParm = "";

        try {

            String oper = dataObj.getOper();
            if (oper.equals("error")) {
                return "";
            }
            if (oper.equals(CALLC_UPDATE)) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 1) {
                    phone = operList[0];
                    // search call control with phone to find the ban

                    if (operList.length > 5) {

                        for (int k = 0; k < operList.length; k++) {
                            String inL = operList[k];
//                          relatePartyList:id:35889253
                            if (inL.indexOf("relatePartyList") != -1) {
                                inL = ServiceAFweb.replaceAll("\"", "", inL);
                                inL = ServiceAFweb.replaceAll("relatePartyList:id:", "", inL);
                                banid = inL;
                            }
//                          callCharacteristicList
                            if (inL.indexOf("callCharacteristicList") != -1) {
                                inL = operList[k + 2]; //value:VOLTE
                                inL = ServiceAFweb.replaceAll("\"", "", inL);
                                inL = ServiceAFweb.replaceAll("value:", "", inL);
                                host = inL;
                            }
                        }

                        dataSt = dataObj.getData();

                        int beg = dataSt.indexOf("{");
                        if (beg != -1) {
                            postParm = dataSt.substring(beg);
                            postParm += "}}]}";
                            //"callerList":[{"callerName":","phoneNumber":"355692727113"} error at :", 
                            postParm = ServiceAFweb.replaceAll(":\",", ":\" \",", postParm);
                            postParm = ServiceAFweb.replaceAll("= ", "", postParm);

                        }
                    }
                    cmd.add("get call control");
                    cmd.add(CALLC_GET);
                    pData.setCmd(cmd);
                }
            } else if (oper.equals(CALLC_GET)) { //"getAppointment")) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 3) {
                    phone = operList[0];
                    banid = operList[1];
                    host = operList[2];
                }
                cmd.add("get call control");
                cmd.add(CALLC_GET);
                pData.setCmd(cmd);
            } else if (oper.equals(CALLC_RESET)) { //"getAppointment")) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 1) {
                    phone = operList[0];
                    // search call control with phone to find the ban

                    if (operList.length > 5) {

                        for (int k = 0; k < operList.length; k++) {
                            String inL = operList[k];
//                          relatePartyList:id:35889253
                            if (inL.indexOf("relatePartyList") != -1) {
                                inL = ServiceAFweb.replaceAll("\"", "", inL);
                                inL = ServiceAFweb.replaceAll("relatePartyList:id:", "", inL);
                                banid = inL;
                            }
//                          callCharacteristicList
                            if (inL.indexOf("callCharacteristicList") != -1) {
                                inL = operList[k + 2]; //value:VOLTE
                                inL = ServiceAFweb.replaceAll("\"", "", inL);
                                inL = ServiceAFweb.replaceAll("value:", "", inL);
                                host = inL;
                            }
                        }

                        dataSt = dataObj.getData();

                        int beg = dataSt.indexOf("{");
                        if (beg != -1) {
                            postParm = dataSt.substring(beg);
                            postParm += "}}]}";
                            //"callerList":[{"callerName":","phoneNumber":"355692727113"} error at :", 
                            postParm = ServiceAFweb.replaceAll(":\",", ":\" \",", postParm);
                            postParm = ServiceAFweb.replaceAll("= ", "", postParm);

                        }
                    }
                    cmd.add("get call control");
                    cmd.add(CALLC_GET);
                    pData.setCmd(cmd);
                }
            } else {
                logger.info("> getFeatureSsnsCallCProcess Other oper " + oper);
                return "";
            }

            if ((banid.length() == 0) || (phone.length() == 0)) {
                return "";
            }
//            logger.info(dataSt);
/////////////
            SsnsAcc NAccObj = new SsnsAcc();
            NAccObj.setDown("splunkflow");

            boolean stat = this.updateSsnsCallC(oper, postParm, banid, phone, host, pData, dataObj, NAccObj);
            if (stat == true) {
//                if (devOPflag == 1) {
//                    String feat = NAccObj.getName() + ":TicktoCust";
//                    NAccObj.setName(feat);
//                }
                boolean exist = false;
                String key = NAccObj.getName()
                        + NAccObj.getCusid()
                        + NAccObj.getBanid()
                        + NAccObj.getTiid();
                key = key.replaceAll(NAccObj.getUid(), "");
                ArrayList<SsnsAcc> ssnsAccObjList = getSsnsDataImp().getSsnsAccObjList(NAccObj.getName(), NAccObj.getUid());
                if (set.add(key)) {
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() != 0) {
                            SsnsAcc ssnsObj = ssnsAccObjList.get(0);
                            if (ssnsObj.getDown().equals("splunkflow")) {
                                exist = true;
                            }
                        }
                    }
                }

                if (exist == false) {
                    ssnsAccObjList = getSsnsDataImp().getSsnsAccObjListByTiid(NAccObj.getName(), NAccObj.getTiid());
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() > 3) {
                            exist = true;
                        }
                    }
                }
                if (exist == false) {
                    int ret = getSsnsDataImp().insertSsnsAccObject(NAccObj);
                }

            }
            return NAccObj.getName();
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsCallCProcess Exception " + ex.getMessage());
        }
        return "";
    }

    public boolean updateSsnsCallC(String oper, String postParm, String banid, String phone, String host, ProductData pData, SsnsData dataObj, SsnsAcc NAccObj) {
        try {
            String featTTV = "";
            String outputSt = null;
            if (oper.equals(CALLC_GET) || oper.equals(CALLC_UPDATE) || oper.equals(CALLC_RESET)) {
                if ((banid.length() == 0) || (phone.length() == 0)) {
                    return false;
                }

                outputSt = SendSsnsCallControl(ServiceAFweb.URL_PRODUCT_PR, banid, phone, host, null);
                if (outputSt == null) {
                    return false;
                }
                if (outputSt.length() < 80) {
                    // special case for no appointment {"status":{"statusCd":"200","statusTxt":"OK"},"appointmentList":[]}
                    return false;
                }
                if (outputSt.indexOf("responseCode:400500") != -1) {
                    return false;
                }
                featTTV = parseCallControlFeature(outputSt, oper, host);

            } else {
                return false;
            }

//            logger.info("> updateSsnsCallC feat " + featTTV);
            if (NAccObj.getDown().equals("splunkflow")) {

                ArrayList<String> flow = new ArrayList();
                int faulure = getSsnsFlowTrace(dataObj, flow);
                if (flow == null) {
                    logger.info("> updateSsnsCallC skip no flow");
                    return false;
                }
                pData.setFlow(flow);

                if (faulure == 1) {
                    featTTV += ":splunkfailed";
                }
            }
            logger.info("> updateSsnsCallC feat " + featTTV);
            pData.setPostParam(postParm);
            NAccObj.setName(featTTV);
            NAccObj.setBanid(banid);
            NAccObj.setCusid(phone);
            NAccObj.setUid(dataObj.getUid());
            NAccObj.setApp(dataObj.getApp());
            NAccObj.setOper(oper);

//          NAccObj.setDown(""); // set by NAccObj
            NAccObj.setRet(host);
            NAccObj.setExec(dataObj.getExec());

            String nameSt = new ObjectMapper().writeValueAsString(pData);
            NAccObj.setData(nameSt);

            NAccObj.setUpdatedatel(dataObj.getUpdatedatel());
            NAccObj.setUpdatedatedisplay(new java.sql.Date(dataObj.getUpdatedatel()));

            return true;
        } catch (Exception ex) {
            logger.info("> updateSsnsCallC Exception " + ex.getMessage());
        }
        return false;
    }

    ////////////////////////////////////////////
    public String getFeatureSsnsActCfg(SsnsData dataObj) {
        String feat = "";
        try {
            feat = getFeatureSsnsActCfgProcess(dataObj);
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsActCfg Exception " + ex.getMessage());
        }
        getSsnsDataImp().updatSsnsDataStatusById(dataObj.getId(), ConstantKey.COMPLETED);
        return feat;
    }

    public String getFeatureSsnsActCfgProcess(SsnsData dataObj) {
        ProductData pData = new ProductData();
        ArrayList<String> cmd = new ArrayList();
        if (dataObj == null) {
            return "";
        }

        String custid = "";
        String consumer = "";
        String service = "";
        String dataSt = "";
        String postParm = "";
        String refId = "";

        try {
            String oper = dataObj.getOper();
            if (oper.equals(ACTCFG_UPDATE_SRV)) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 1) {
                    refId = operList[0];

                    consumer = "C";
                    service = "email";

                    if (operList.length > 5) {
                        dataSt = dataObj.getData();

                        int beg = dataSt.indexOf("{");
                        if (beg != -1) {
                            postParm = dataSt.substring(beg);
                            postParm += "}";
                            postParm = ServiceAFweb.replaceAll("}]}]}", "}]}", postParm);
                            //[{""id"":""86062848"",""name"":""customerid""}
                            String[] parmList = postParm.split(",");
                            for (int j = 0; j < parmList.length; j++) {
                                String inLine = parmList[j];
                                if (inLine.indexOf("customerid") != -1) {
                                    String value = parmList[j - 1];
                                    //"relatedPartyList":[{"id":"20006858"
                                    value = ServiceAFweb.replaceAll("\"", "", value);
                                    value = ServiceAFweb.replaceAll("relatedPartyList:[{id:", "", value);
                                    custid = value;
                                    break;
                                }

                            }

                        }
                    }
                    cmd.add("get email");
                    cmd.add(ACTCFG_GET_SRV);
                    pData.setCmd(cmd);
                }
            } else if (oper.equals(ACTCFG_GET_SRV)) { //"getAppointment")) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 2) {
                    custid = operList[0];
                    consumer = operList[1];
                    service = operList[2];

                }
                cmd.add("get email");
                cmd.add(ACTCFG_GET_SRV);
                pData.setCmd(cmd);

            } else {
                logger.info("> getFeatureSsnsActCfgProcess Other oper " + oper);
            }
            if (oper.equals(ACTCFG_GET_SRV)) {
                // for testing ignore APP_GET_APP becase alwasy no info
//                return "";
                // for testing
            } else if (oper.equals(ACTCFG_UPDATE_SRV)) {

            } else {
                logger.info(dataSt);
                return "";
            }
//            logger.info(dataSt);
/////////////
            SsnsAcc NAccObj = new SsnsAcc();
            NAccObj.setDown("splunkflow");

            boolean stat = this.updateSsnsActCfg(oper, postParm, custid, consumer, service, refId, pData, dataObj, NAccObj);
            if (stat == true) {
//                if (devOPflag == 1) {
//                    String feat = NAccObj.getName() + ":TicktoCust";
//                    NAccObj.setName(feat);
//                }
                boolean exist = false;
                String key = NAccObj.getName()
                        + NAccObj.getCusid()
                        + NAccObj.getBanid()
                        + NAccObj.getTiid();
                key = key.replaceAll(NAccObj.getUid(), "");
                ArrayList<SsnsAcc> ssnsAccObjList = getSsnsDataImp().getSsnsAccObjList(NAccObj.getName(), NAccObj.getUid());
                if (set.add(key)) {
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() != 0) {
                            SsnsAcc ssnsObj = ssnsAccObjList.get(0);
                            if (ssnsObj.getDown().equals("splunkflow")) {
                                exist = true;
                            }
                        }
                    }
                }

                if (exist == false) {
                    ssnsAccObjList = getSsnsDataImp().getSsnsAccObjListByTiid(NAccObj.getName(), NAccObj.getTiid());
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() > 3) {
                            exist = true;
                        }
                    }
                }
                if (exist == false) {
                    int ret = getSsnsDataImp().insertSsnsAccObject(NAccObj);
                }

            }
            return NAccObj.getName();
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsActCfgProcess Exception " + ex.getMessage());
        }
        return "";
    }

    public boolean updateSsnsActCfg(String oper, String postParm, String custid, String consumer, String service, String refId, ProductData pData, SsnsData dataObj, SsnsAcc NAccObj) {
        try {
            String featTTV = "";
            String outputSt = null;
            if (oper.equals(ACTCFG_GET_SRV) || oper.equals(ACTCFG_UPDATE_SRV)) {
                if ((custid.length() == 0) || (consumer.length() == 0)) {
                    return false;
                }

                outputSt = SendSsnsActCfg(ServiceAFweb.URL_PRODUCT_PR, custid, consumer, service, null);
                if (outputSt == null) {
                    return false;
                }
                if (outputSt.length() < 80) {
                    // special case for no appointment {"status":{"statusCd":"200","statusTxt":"OK"},"appointmentList":[]}
                    return false;
                }
                if (outputSt.indexOf("responseCode:400500") != -1) {
                    return false;
                }
                featTTV = parseActCfgFeature(outputSt, oper, service);

            } else {
                return false;
            }

//            logger.info("> updateSsnsCallC feat " + featTTV);
            if (NAccObj.getDown().equals("splunkflow")) {

                ArrayList<String> flow = new ArrayList();
                int faulure = getSsnsFlowTrace(dataObj, flow);
                if (flow == null) {
                    logger.info("> updateSsnsActCfg skip no flow");
                    return false;
                }
                pData.setFlow(flow);

                if (faulure == 1) {
                    featTTV += ":splunkfailed";
                }
            }
            logger.info("> updateSsnsActCfg feat " + featTTV);
            pData.setPostParam(postParm);
            NAccObj.setName(featTTV);
            NAccObj.setBanid(dataObj.getBanid());
            NAccObj.setCusid(custid);
            NAccObj.setTiid(refId);
            NAccObj.setUid(dataObj.getUid());
            NAccObj.setApp(dataObj.getApp());
            NAccObj.setOper(oper);

//          NAccObj.setDown(""); // set by NAccObj
            NAccObj.setRet(service);
            NAccObj.setExec(dataObj.getExec());

            String nameSt = new ObjectMapper().writeValueAsString(pData);
            NAccObj.setData(nameSt);

            NAccObj.setUpdatedatel(dataObj.getUpdatedatel());
            NAccObj.setUpdatedatedisplay(new java.sql.Date(dataObj.getUpdatedatel()));

            return true;
        } catch (Exception ex) {
            logger.info("> updateSsnsCallC Exception " + ex.getMessage());
        }
        return false;
    }

//    public static HashMap<String, String> actCfgMap = new HashMap<String, String>();
    public static String parseActCfgFeature(String outputSt, String oper, String service) {

        if (outputSt == null) {
            return "";
        }
        int userName = 0;
        int name = 0;
        ArrayList<String> outputList = ServiceAFweb.prettyPrintJSON(outputSt);
        for (int j = 0; j < outputList.size(); j++) {
            String inLine = outputList.get(j);
//            logger.info("" + inLine);

            //"refId": "13685041",
//            if (custid.length() > 0) {
//                if (inLine.indexOf("refId") != -1) {
//                    String valueSt = ServiceAFweb.replaceAll("\"", "", inLine);
//                    valueSt = ServiceAFweb.replaceAll("refId:", "", valueSt);
//                    valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
//                    actCfgMap.put(valueSt, custid);
//                    continue;
//                }
//            }
            if (inLine.indexOf("userName") != -1) {
                userName++;
                continue;
            }
            if (inLine.indexOf("name") != -1) {
                name++;
                continue;
            }

            //"refId": "13825353", save refid to customer id for future use
        }

        String featTTV = APP_FEAT_TYPE_ACTCFG;
        featTTV += ":" + oper;
        featTTV += ":" + service;
        featTTV += ":" + "userName_" + userName;
        featTTV += ":" + "name_" + name;
        return featTTV;
    }

    public String SendSsnsActCfg(String ProductURL, String custid, String consumer, String service, ArrayList<String> inList) {
        logger.info("> SendSsnsActCfg " + custid + " " + consumer + " " + service);
        String url = "";
        if ((custid.length() == 0) || (consumer.length() == 0) || (service.length() == 0)) {
            return null;
        }

        url = ProductURL + "/service/cmsServiceActivationAndConfiguration/v1/service?servicetype=" + service
                + "&relatedpartylist.customerid=" + custid
                + "&relatedpartylist.accounttype=" + consumer;

        try {
            if (inList != null) {
                inList.add(url);
            }
            // calculate elapsed time in milli seconds
            long startTime = TimeConvertion.currentTimeMillis();

            String output = this.sendRequest_Ssns(METHOD_GET, url, null, null, null);

            long endTime = TimeConvertion.currentTimeMillis();
            long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
            if (inList != null) {
                String tzid = "America/New_York"; //EDT
                TimeZone tz = TimeZone.getTimeZone(tzid);
                Date d = new Date(startTime);
                // timezone symbol (z) included in the format pattern 
                DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                // format date in target timezone
                format.setTimeZone(tz);
                String ESTdate = format.format(d);

                inList.add(ESTdate + " elapsedTime:" + elapsedTime);
                inList.add("output:");
            }

            return output;
        } catch (Exception ex) {
            logger.info("> SsnsProdiuctInventory exception " + ex.getMessage());
        }
        return null;
    }

////////////////////////////////////////////    
///////////////////////////////////////////    
    public String getFeatureSsnsWLNPro(SsnsData dataObj) {
        String feat = "";
        try {
            feat = getFeatureSsnsWLNProProcess(dataObj);
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsWLNPro Exception " + ex.getMessage());
        }
        getSsnsDataImp().updatSsnsDataStatusById(dataObj.getId(), ConstantKey.COMPLETED);
        return feat;
    }

    public String getFeatureSsnsWLNProProcess(SsnsData dataObj) {
        ProductData pData = new ProductData();
        ArrayList<String> cmd = new ArrayList();
        if (dataObj == null) {
            return "";
        }
        String custid = "";
        String bundleName = "";
        String serviceType = "";
        String skuP = "";
        String catalogId = "";

        String postParm = "";

        String dataSt = "";
        try {
            String oper = dataObj.getOper();
            if (oper.equals(APP_GET_DOWNURL)) {

                dataSt = dataObj.getData();
                if (dataSt.indexOf("CustomerInfo") != -1) {
                    dataSt = ServiceAFweb.replaceAll("CustomerInfo", "", dataSt);

                    String[] operList = dataSt.split(",");
                    String custSt = operList[0];
                    if (custSt.indexOf("[customerId=") != -1) {
                        custSt = custSt.replace("[customerId=", "");
                        custid = custSt.trim();
                    }

                    ArrayList<SsnsData> ssnsList = getSsnsDataImp().getSsnsDataObjListByUid(dataObj.getApp(), dataObj.getUid());
                    for (int j = 0; j < ssnsList.size(); j++) {
                        SsnsData ssnsDataObj = ssnsList.get(j);
                        String dataParam = ssnsDataObj.getData();
                        if (dataParam.indexOf("ProtectionSubscriptionInfo") != -1) {
                            dataParam = ServiceAFweb.replaceAll("ProtectionSubscriptionInfo", "", dataParam);

                            operList = dataParam.split(",");
                            for (int k = 0; k < operList.length; k++) {
                                String lineSt = operList[k];
                                if (lineSt.indexOf("[catalogId=") != -1) {
                                    lineSt = lineSt.replace("[catalogId=", "");
                                    catalogId = lineSt.trim();
                                }
                                if (lineSt.indexOf("bundleName=") != -1) {
                                    lineSt = lineSt.replace("bundleName=", "");
                                    bundleName = lineSt.trim();
                                    bundleName = ServiceAFweb.replaceAll(" ", "_", bundleName);
                                }
                                if (lineSt.indexOf("serviceType=") != -1) {
                                    lineSt = lineSt.replace("serviceType=", "");
                                    serviceType = lineSt.replace("]", "");
                                    serviceType = serviceType.trim();
                                }
                                if (lineSt.indexOf("skuP=") != -1) {
                                    lineSt = lineSt.replace("skuP=", "");
                                    skuP = lineSt.trim();
                                }
                            }
                        }
                    }

                } else {
                    return "";
                }

                cmd.add("get download URL");
                cmd.add(APP_GET_DOWNURL);
                pData.setCmd(cmd);

            } else {
                logger.info("> getFeatureSsnsWLNProProcess Other oper " + oper);
                return "";
            }

            if (custid.equals("")) {
                return "";
            }

//            logger.info(dataSt);
/////////////
            //call devop to get customer id
            SsnsAcc NAccObj = new SsnsAcc();
            NAccObj.setDown("splunkflow");
            boolean stat = this.updateSsnsWLNPro(oper, custid, catalogId, bundleName, serviceType, skuP, pData, dataObj, NAccObj);
            if (stat == true) {
                boolean exist = false;
                String key = NAccObj.getName()
                        + NAccObj.getCusid()
                        + NAccObj.getBanid()
                        + NAccObj.getTiid();
                key = key.replaceAll(NAccObj.getUid(), "");
                ArrayList<SsnsAcc> ssnsAccObjList = getSsnsDataImp().getSsnsAccObjList(NAccObj.getName(), NAccObj.getUid());
                if (set.add(key)) {
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() != 0) {
                            SsnsAcc ssnsObj = ssnsAccObjList.get(0);
                            if (ssnsObj.getDown().equals("splunkflow")) {
                                exist = true;
                            }
                        }
                    }
                }

                if (exist == false) {
                    ssnsAccObjList = getSsnsDataImp().getSsnsAccObjListByBan(NAccObj.getName(), NAccObj.getBanid());
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() > 3) {
                            exist = true;
                        }
                    }
                }
                if (exist == false) {
                    int ret = getSsnsDataImp().insertSsnsAccObject(NAccObj);
                }
            }
            return NAccObj.getName();
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsTTVCProcess Exception " + ex.getMessage());
        }
        return "";
    }

    public boolean updateSsnsWLNPro(String oper, String custid, String catalogId, String bundleName,
            String serviceType, String skuP, ProductData pData, SsnsData dataObj, SsnsAcc NAccObj) {
        try {
            String featTTV = "";

            String outputSt = null;

            outputSt = SendSsnsWLNPro(ServiceAFweb.URL_PRODUCT_PR, APP_GET_DOWNURL, custid, serviceType, skuP, null);
            if (outputSt == null) {
                return false;
            }
            if (outputSt == null) {
                return false;
            }
            if (outputSt.length() < 80) {
                // special case for no appointment {"status":{"statusCd":"200","statusTxt":"OK"},"appointmentList":[]}
                return false;
            }
            if (outputSt.indexOf("responseCode:400500") != -1) {
                return false;
            }
            featTTV = parseWLNProFeature(outputSt, oper, bundleName);

            int failure = 0;
            if (NAccObj.getDown().equals("splunkflow")) {

                ArrayList<String> flow = new ArrayList();
                failure = getSsnsFlowTrace(dataObj, flow);
                if (flow == null) {
                    logger.info("> updateSsnsWLNPro skip no flow");
                    return false;
                }
                pData.setFlow(flow);
            }

            if (failure == 1) {
                featTTV += ":splunkfailed";
            }

            logger.info("> updateSsnsWLNPro feat " + featTTV);
            pData.setPostParam("");
            NAccObj.setName(featTTV);
            NAccObj.setBanid("");
            NAccObj.setCusid(custid);
            String tid = catalogId + ":" + serviceType + ":" + skuP + ":" + bundleName;
            NAccObj.setTiid(tid);

            NAccObj.setUid(dataObj.getUid());
            NAccObj.setApp(APP_WLNPRO);
            NAccObj.setOper(oper);

//          NAccObj.setDown(""); // set by NAccObj
            NAccObj.setRet(dataObj.getRet());
            NAccObj.setExec(dataObj.getExec());

            String nameSt = new ObjectMapper().writeValueAsString(pData);
            NAccObj.setData(nameSt);

            NAccObj.setUpdatedatel(dataObj.getUpdatedatel());
            NAccObj.setUpdatedatedisplay(new java.sql.Date(dataObj.getUpdatedatel()));

            return true;
        } catch (Exception ex) {
            logger.info("> updateSsnsWLNPro Exception " + ex.getMessage());
        }
        return false;
    }

    public static String parseWLNProFeature(String outputSt, String oper, String bundleName) {

        if (outputSt == null) {
            return "";
        }
        String serviceTypeCd = "";
        String skuTxt = "";
        String licenceCount = "";

        ArrayList<String> outputList = ServiceAFweb.prettyPrintJSON(outputSt);
        for (int j = 0; j < outputList.size(); j++) {
            String inLine = outputList.get(j);
//            logger.info("" + inLine);
            if (inLine.indexOf("serviceTypeCd") != -1) {
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("serviceTypeCd:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                serviceTypeCd = valueSt;
                continue;
            }
            if (inLine.indexOf("skuTxt") != -1) {
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("skuTxt:", "", valueSt);
                skuTxt = ServiceAFweb.replaceAll(",", "", valueSt);
                continue;
            }

            if (inLine.indexOf("licenceCount") != -1) {
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("licenceCount:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                licenceCount = valueSt;
            }
        }

        String featTTV = APP_FEAT_TYPE_WLNP;
        featTTV += ":" + oper;
        featTTV += ":" + bundleName;
        featTTV += ":" + serviceTypeCd + ":" + skuTxt + ":licenceCount_" + licenceCount;
        return featTTV;
    }

    public String SendSsnsWLNPro(String ProductURL, String oper, String custid, String serviceType, String skuP, ArrayList<String> inList) {
        logger.info("> SendSsnsWLNPro " + oper + " " + custid + " " + serviceType);

        String url = "";
        try {
            url = ProductURL + "/v2/cmo/selfmgmt/wirelineprotectionsubscriptionservice/customer/" + custid
                    + "/protection/email/download-urls";

            String postParm = "{"
                    + "  \"customerId\": \"xxcustid\","
                    + "  \"languageCd\": \"EN\","
                    + "  \"emailAddressList\": [],"
                    + "  \"auditInfo\": {"
                    + "    \"originatorApplicationId\": \"APP_SELFSERVEUSGBIZSVC\""
                    + "  },"
                    + "  \"componentDetailList\": ["
                    + "    {"
                    + "      \"serviceTypeCd\": \"xxserv\","
                    + "      \"skuTxt\": \"xxsku\""
                    + "    }"
                    + "  ]"
                    + "}";
            postParm = postParm.replace("xxcustid", custid);
            postParm = postParm.replace("xxserv", serviceType);
            postParm = postParm.replace("xxsku", skuP);

//            HashMap newbodymap = new ObjectMapper().readValue(postParm, HashMap.class);
            if (inList != null) {
                inList.add(url);
            }
            // calculate elapsed time in milli seconds
            long startTime = TimeConvertion.currentTimeMillis();

            String output = this.sendRequest_Ssns(METHOD_POST, url, null, null, postParm);

            long endTime = TimeConvertion.currentTimeMillis();
            long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
            if (inList != null) {
                String tzid = "America/New_York"; //EDT
                TimeZone tz = TimeZone.getTimeZone(tzid);
                Date d = new Date(startTime);
                // timezone symbol (z) included in the format pattern 
                DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                // format date in target timezone
                format.setTimeZone(tz);
                String ESTdate = format.format(d);

                inList.add(ESTdate + " elapsedTime:" + elapsedTime);
                inList.add("bodyElement:" + postParm);
                inList.add("output:");
            }
            return output;

        } catch (Exception ex) {
            logger.info("> SendSsnsWLNPro exception " + ex.getMessage());
        }
        return null;
    }

    public String TestFeatureSsnsProdWLNPro(SsnsAcc dataObj, ArrayList<String> outputList, String oper, String LABURL) {
        if (dataObj == null) {
            return "";
        }
        dataObj.getData();
        String custid = dataObj.getCusid();
        String appTId = dataObj.getTiid();
        if (appTId.length() == 0) {
            return "";
        }
        String WifiparL[] = appTId.split(":");
        String serviceType = WifiparL[1];
        String skuP = WifiparL[2];
        String bundleName = WifiparL[3];

        if (LABURL.length() == 0) {
            LABURL = ServiceAFweb.URL_PRODUCT_PR;
        }

        String outputSt = null;
        ArrayList<String> inList = new ArrayList();
        if (oper.equals(APP_GET_DOWNURL)) {
            outputSt = SendSsnsWLNPro(ServiceAFweb.URL_PRODUCT_PR, APP_GET_DOWNURL, custid, serviceType, skuP, inList);

            if (outputSt == null) {
                return "";
            }
            ////special char #, need to ignore for this system
            outputSt = outputSt.replaceAll("#", "");
            outputSt = outputSt.replaceAll("~", "");
            outputSt = outputSt.replaceAll("^", "");

            ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);
            String feat = parseWLNProFeature(outputSt, oper, bundleName);

            if (outputSt.indexOf("responseCode:400500") != -1) {
                feat += ":testfailed";
            }
            outputList.add(feat);
            outputList.addAll(inList);
            outputList.addAll(outList);

            return feat;
        }

        return "";
    }

////////////////////////////////////////////    
    public String getFeatureSsnsTTVC(SsnsData dataObj) {
        String feat = "";
        try {
            feat = getFeatureSsnsTTVCProcess(dataObj);
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsTTVC Exception " + ex.getMessage());
        }
        getSsnsDataImp().updatSsnsDataStatusById(dataObj.getId(), ConstantKey.COMPLETED);
        return feat;
    }

    public String getFeatureSsnsTTVCProcess(SsnsData dataObj) {
        ProductData pData = new ProductData();
        ArrayList<String> cmd = new ArrayList();
        if (dataObj == null) {
            return "";
        }

        String banid = "";
        String prodid = "";

        String postParm = "";

        String dataSt = "";
        try {
            String oper = dataObj.getOper();
            if ((oper.equals(TT_Vadulate) || oper.equals(TT_Quote) || oper.equals(TT_SaveOrder))) { //"updateAppointment")) {

                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 1) {
                    banid = operList[0];
                    prodid = operList[1];

                    if (operList.length > 5) {
                        dataSt = dataObj.getData();

                        int beg = dataSt.indexOf("{");
                        if (beg != -1) {
                            postParm = dataSt.substring(beg);
                            postParm += "}";
                            postParm = ServiceAFweb.replaceAll(":\",", ":\" \",", postParm);
                            postParm = ServiceAFweb.replaceAll("= ", "", postParm);
                            postParm = ServiceAFweb.replaceAll("}]}", "}", postParm);
                        }
                    }
                }
                cmd.add("get customer ttv subscription");
                cmd.add(TT_GetSub);
                cmd.add("ttv validate");
                cmd.add(TT_Vadulate);
                cmd.add("ttv quotation");
                cmd.add(TT_Quote);
                pData.setCmd(cmd);

            } else if (oper.equals(TT_GetSub)) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 1) {
                    banid = operList[0];
                    prodid = operList[1];
                }
                cmd.add("get customer ttv subscription");
                cmd.add(TT_GetSub);
                pData.setCmd(cmd);

            } else {
                logger.info("> getFeatureSsnsTTVCProcess Other oper " + oper);
                return "";
            }

            if (prodid.equals("")) {
                return "";
            }

//            logger.info(dataSt);
/////////////
            //call devop to get customer id
            SsnsAcc NAccObj = new SsnsAcc();
            NAccObj.setDown("splunkflow");
            boolean stat = this.updateSsnsTTVC(oper, banid, prodid, postParm, pData, dataObj, NAccObj);
            if (stat == true) {
                boolean exist = false;
                String key = NAccObj.getName()
                        + NAccObj.getCusid()
                        + NAccObj.getBanid()
                        + NAccObj.getTiid();
                key = key.replaceAll(NAccObj.getUid(), "");
                ArrayList<SsnsAcc> ssnsAccObjList = getSsnsDataImp().getSsnsAccObjList(NAccObj.getName(), NAccObj.getUid());
                if (set.add(key)) {
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() != 0) {
                            SsnsAcc ssnsObj = ssnsAccObjList.get(0);
                            if (ssnsObj.getDown().equals("splunkflow")) {
                                exist = true;
                            }
                        }
                    }
                }

                if (exist == false) {
                    ssnsAccObjList = getSsnsDataImp().getSsnsAccObjListByBan(NAccObj.getName(), NAccObj.getBanid());
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() > 3) {
                            exist = true;
                        }
                    }
                }
                if (exist == false) {
                    int ret = getSsnsDataImp().insertSsnsAccObject(NAccObj);
                }
            }
            return NAccObj.getName();
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsTTVCProcess Exception " + ex.getMessage());
        }
        return "";
    }

    public boolean updateSsnsTTVC(String oper, String banid, String prodid, String postParm, ProductData pData, SsnsData dataObj, SsnsAcc NAccObj) {
        try {
            String featTTV = "";
            int legacyDiscount = 0;
            int XQException = 0;
            if (oper.equals(TT_GetSub) || oper.equals(TT_Vadulate) || oper.equals(TT_Quote) || oper.equals(TT_SaveOrder)) {
                if ((banid.length() == 0) && (prodid.length() == 0)) {
                    return false;
                } else {
                    String outputSt = null;
                    outputSt = SendSsnsTTVC(ServiceAFweb.URL_PRODUCT_PR, TT_GetSub, banid, prodid, postParm, null);
                    if (outputSt == null) {
                        return false;
                    }
                    if (outputSt.length() == 0) {
                        return false;
                    }
//                    if (outputSt.length() < 80) {  // or test 
//                        return false;
//                    }

                    if (outputSt.indexOf("Legacy Discount") != -1) {
                        String dataSt = ServiceAFweb.replaceAll("\"", "", outputSt);
                        if (dataSt.indexOf("statusCd:400") != -1) {
                            legacyDiscount = 1;
                        }
                    }

                    if (outputSt.indexOf("responseCode:400500") != -1) {
                        if (outputSt.indexOf("XQException") != -1) {
                            XQException = 1;
                        } else {
                            return false;
                        }
                    }
                    featTTV = parseTTVCFeature(outputSt, oper, postParm);
                }
            } else {
                return false;
            }

//            logger.info("> updateSsnsTTVC feat " + featTTV);
/////////////TTV   
            int failure = 0;
            if (NAccObj.getDown().equals("splunkflow")) {

                ArrayList<String> flow = new ArrayList();
                failure = getSsnsFlowTrace(dataObj, flow);
                if (flow == null) {
                    logger.info("> updateSsnsTTVC skip no flow");
                    return false;
                }
                pData.setFlow(flow);

            }

            if (legacyDiscount == 1) {
                featTTV += ":legacyDisc:failed";
                if (failure == 0) {
                    featTTV += ":splunkfailed";
                }
            }
            if (XQException == 1) {
                featTTV += ":XQException";
                if (failure == 0) {
                    featTTV += ":splunkfailed";
                }
            }
            if (failure == 1) {
                featTTV += ":splunkfailed";
            }

            logger.info("> updateSsnsTTVC feat " + featTTV);
            pData.setPostParam(postParm);
            NAccObj.setName(featTTV);
            NAccObj.setBanid(banid);
            NAccObj.setCusid(dataObj.getCusid());

            NAccObj.setTiid(prodid);

            NAccObj.setUid(dataObj.getUid());
            NAccObj.setApp(APP_TTVC);
            NAccObj.setOper(oper);

//          NAccObj.setDown(""); // set by NAccObj
            NAccObj.setRet(dataObj.getRet());
            NAccObj.setExec(dataObj.getExec());

            String nameSt = new ObjectMapper().writeValueAsString(pData);
            NAccObj.setData(nameSt);

            NAccObj.setUpdatedatel(dataObj.getUpdatedatel());
            NAccObj.setUpdatedatedisplay(new java.sql.Date(dataObj.getUpdatedatel()));

            return true;
        } catch (Exception ex) {
            logger.info("> updateSsnsAppointment Exception " + ex.getMessage());
        }
        return false;
    }

    public static String parseTTVCFeature(String outputSt, String oper, String postParm) {

        if (outputSt == null) {
            return "";
        }

        int packCd = 0;
        int channelCd = 0;
        int discountCd = 0;
        int add = 0;
        int remove = 0;

        String geomarket = "";
        String offer = "noOfferCd";
        String collectionCd = "";

        ArrayList<String> outputList = ServiceAFweb.prettyPrintJSON(outputSt);
        for (int j = 0; j < outputList.size(); j++) {
            String inLine = outputList.get(j);
//            logger.info("" + inLine);
            if (inLine.indexOf("geoTargetMarket") != -1) {
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("geoTargetMarket:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                geomarket = valueSt;
            }
            if (inLine.indexOf("offerCd") != -1) {
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("offerCd:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);

                if (valueSt.indexOf("MediaroomTV-HS2.0") != -1) {
                    offer = "Mediaroom20";
                    continue;
                } else if (valueSt.indexOf("MediaroomTV-HS") != -1) {
                    offer = "Mediaroom";
                    continue;
                } else if (valueSt.indexOf("TVX") != -1) {
                    offer = "TVX";
                }
                continue;
            }

            if (inLine.indexOf("collectionCd") != -1) {
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("collectionCd:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                collectionCd = valueSt;
            }

            if (inLine.indexOf("discountCd") != -1) {
                discountCd++;
                continue;
            }
            if (inLine.indexOf("packCd") != -1) {
                packCd++;
                continue;
            }
            if (inLine.indexOf("channelCd") != -1) {
                channelCd++;
                continue;
            }
        }

        if (postParm != null) {
            if (postParm.length() > 0) {
                outputList = ServiceAFweb.prettyPrintJSON(postParm);
                for (int j = 0; j < outputList.size(); j++) {
                    String inLine = outputList.get(j);
                    if (inLine.indexOf("ADD") != -1) {
                        add++;
                        continue;
                    }
                    if (inLine.indexOf("REMOVE") != -1) {
                        remove++;
                        continue;
                    }
                }
            }
        }

        String featTTV = APP_FEAT_TYPE_TTVCL;
        featTTV += ":" + oper;

//        String gm = geomarket;
//        if (gm.length() == 0) {
//            gm = "noGeoMarket";
//        }
//        featTTV += ":" + gm;
        featTTV += ":" + offer;
        String coll = collectionCd;
        if (coll.length() == 0) {
            coll = "Essential";
        }
        featTTV += ":" + coll;
        featTTV += ":Pack_" + packCd;
        featTTV += ":Channel_" + channelCd;
        featTTV += ":Disc_" + discountCd;
        if (add == 0) {
            featTTV += ":NAdd";
        } else if (add < 3) {
            featTTV += ":Add";
        } else {
            featTTV += ":Add_" + add;
        }
        if (remove == 0) {
            featTTV += ":NRemove";
        } else if (remove < 3) {
            featTTV += ":Remove";
        } else {
            featTTV += ":Remove_" + remove;
        }
        return featTTV;
    }

    public String SendSsnsTTVC(String ProductURL, String oper, String banid, String prodid, String postParm, ArrayList<String> inList) {
        logger.info("> SendSsnsTTVC " + oper + " " + banid + " " + prodid);

        String url = "";

        try {
            if (oper.equals(TT_GetSub)) {
                url = ProductURL + "/v1/cmo/selfmgmt/tvsubscription/account/" + banid
                        + "/productinstance/" + prodid
                        + "/subscription";

                if (inList != null) {
                    inList.add(url);
                }
                // calculate elapsed time in milli seconds
                long startTime = TimeConvertion.currentTimeMillis();

                String output = this.sendRequest_Ssns(METHOD_GET, url, null, null, null);

                long endTime = TimeConvertion.currentTimeMillis();
                long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
                if (inList != null) {
                    String tzid = "America/New_York"; //EDT
                    TimeZone tz = TimeZone.getTimeZone(tzid);
                    Date d = new Date(startTime);
                    // timezone symbol (z) included in the format pattern 
                    DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                    // format date in target timezone
                    format.setTimeZone(tz);
                    String ESTdate = format.format(d);

                    inList.add(ESTdate + " elapsedTime:" + elapsedTime);
                    inList.add("output:");
                }
                return output;
            } else if (oper.equals(TT_Quote) || oper.equals(TT_SaveOrder)) {
                url = ProductURL + "/v1/cmo/selfmgmt/tv/requisition/account/" + banid
                        + "/productinstance/" + prodid
                        + "/quotation";

                if (inList != null) {
                    inList.add(url);
                }
                // calculate elapsed time in milli seconds
                long startTime = TimeConvertion.currentTimeMillis();
                String st = postParm;
//                String st = ServiceAFweb.replaceAll("\":\",", "\":\"\",", postParm);
//                st = st.substring(0, st.length() - 2);

                Map<String, String> map = new ObjectMapper().readValue(st, Map.class
                );
                map.remove("customerEmail");

                String output = this.sendRequest_Ssns(METHOD_POST, url, null, map, null);

                long endTime = TimeConvertion.currentTimeMillis();
                long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
                if (inList != null) {
                    String tzid = "America/New_York"; //EDT
                    TimeZone tz = TimeZone.getTimeZone(tzid);
                    Date d = new Date(startTime);
                    // timezone symbol (z) included in the format pattern 
                    DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                    // format date in target timezone
                    format.setTimeZone(tz);
                    String ESTdate = format.format(d);

                    inList.add(ESTdate + " elapsedTime:" + elapsedTime);

                    String bodyElement = new ObjectMapper().writeValueAsString(map);
                    inList.add("bodyElement:" + bodyElement);
                    inList.add("output:");

                }
                return output;

            } else if (oper.equals(TT_Vadulate)) {
                url = ProductURL + "/v1/cmo/selfmgmt/tv/requisition/account/" + banid
                        + "/productinstance/" + prodid
                        + "/validation";

                if (inList != null) {
                    inList.add(url);
                }
                // calculate elapsed time in milli seconds
                long startTime = TimeConvertion.currentTimeMillis();
                String st = postParm;
//                String st = ServiceAFweb.replaceAll("\":\",", "\":\"\",", postParm);
//                st = st.substring(0, st.length() - 2);

                Map<String, String> map = new ObjectMapper().readValue(st, Map.class
                );
                map.remove("customerEmail");

                String output = this.sendRequest_Ssns(METHOD_POST, url, null, map, null);

                long endTime = TimeConvertion.currentTimeMillis();
                long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
                if (inList != null) {
                    String tzid = "America/New_York"; //EDT
                    TimeZone tz = TimeZone.getTimeZone(tzid);
                    Date d = new Date(startTime);
                    // timezone symbol (z) included in the format pattern 
                    DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                    // format date in target timezone
                    format.setTimeZone(tz);
                    String ESTdate = format.format(d);

                    inList.add(ESTdate + " elapsedTime:" + elapsedTime);

                    String bodyElement = new ObjectMapper().writeValueAsString(map);
                    inList.add("bodyElement:" + bodyElement);
                    inList.add("output:");
                }
                return output;
            }
        } catch (Exception ex) {
            logger.info("> SendSsnsTTVC exception " + ex.getMessage());
        }
        return null;
    }

    public String TestFeatureSsnsProdTTVC(SsnsAcc dataObj, ArrayList<String> outputList, String oper, String LABURL) {
        if (dataObj == null) {
            return "";
        }

        dataObj.getData();
        String banid = dataObj.getBanid();
        String appTId = dataObj.getTiid();
        if (appTId.length() == 0) {
            return "";
        }
        if (LABURL.length() == 0) {
            LABURL = ServiceAFweb.URL_PRODUCT_PR;
        }

        String outputSt = null;
        ArrayList<String> inList = new ArrayList();
        if (oper.equals(TT_SaveOrder) || oper.equals(TT_Vadulate) || oper.equals(TT_Quote) || oper.equals(TT_SaveOrder)) {
            outputSt = SendSsnsTTVC(LABURL, TT_GetSub, banid, appTId, null, inList);
            if (outputSt == null) {
                return "";
            }
            ////special char #, need to ignore for this system
            outputSt = outputSt.replaceAll("#", "");
            outputSt = outputSt.replaceAll("~", "");
            outputSt = outputSt.replaceAll("^", "");

            ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);
            String feat = parseTTVCFeature(outputSt, oper, null);
            if (outputSt.indexOf("responseCode:400500") != -1) {
                feat += ":testfailed";
            }

            outputList.add(feat);
            ProductData pData = null;
            String output = dataObj.getData();

            try {
                pData = new ObjectMapper().readValue(output, ProductData.class
                );
            } catch (IOException ex) {
            }
            if (pData == null) {
                return "";
            }
            inList.clear();

            String postParamSt = ProductDataHelper.getPostParamRestore(pData.getPostParam());
            outputSt = SendSsnsTTVC(LABURL, oper, banid, appTId, postParamSt, inList);
            if (outputSt.indexOf("responseCode:400500") != -1) {
                feat += ":testfailed";
                outputList.remove(0);
                outputList.add(0, feat);
            }
            outList = ServiceAFweb.prettyPrintJSON(outputSt);

            outputList.addAll(inList);
            outputList.addAll(outList);

            return feat;
        } else if (oper.equals(TT_GetSub)) {

            outputSt = SendSsnsTTVC(LABURL, oper, banid, appTId, null, inList);;
            if (outputSt == null) {
                return "";
            }
            ////special char #, need to ignore for this system
            outputSt = outputSt.replaceAll("#", "");
            outputSt = outputSt.replaceAll("~", "");
            outputSt = outputSt.replaceAll("^", "");

            ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);
            String feat = parseTTVCFeature(outputSt, oper, null);
            if (outputSt.indexOf("responseCode:400500") != -1) {
                feat += ":testfailed";
            }
            outputList.add(feat);
            outputList.addAll(inList);
            outputList.addAll(outList);

            return feat;
        }

        return "";
    }

////////////////////////////////////////////    
    public String getFeatureSsnsWifi(SsnsData dataObj) {
        String feat = "";
        try {
            feat = getFeatureSsnsWifiProcess(dataObj);
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsWifi Exception " + ex.getMessage());
        }
        getSsnsDataImp().updatSsnsDataStatusById(dataObj.getId(), ConstantKey.COMPLETED);
        return feat;
    }

    public String getFeatureSsnsWifiProcess(SsnsData dataObj) {
        ProductData pData = new ProductData();
        ArrayList<String> cmd = new ArrayList();
        if (dataObj == null) {
            return "";
        }

        String banid = "";
        String uniquid = "";
        String prodClass = "";
        String serialid = "";
        String parm = "";
        String postParm = "";
        int async = 0;
        String dataSt = "";
        try {
            String oper = dataObj.getOper();
            if (oper.equals(WI_GetDeviceStatus)) { //"updateAppointment")) {

                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 3) {
                    banid = operList[0];
                    uniquid = operList[1];
                    prodClass = operList[2];
                    serialid = operList[3];
                    parm = operList[4];
                    if (operList.length > 5) {
                        dataSt = dataObj.getData();

                        int beg = dataSt.indexOf("{");
                        if (beg != -1) {
                            postParm = dataSt.substring(beg);
                            postParm += "}";
                            postParm = ServiceAFweb.replaceAll(":\",", ":\" \",", postParm);
                            postParm = ServiceAFweb.replaceAll("= ", "", postParm);
                            if (dataSt.indexOf("asynchronousRequest") != -1) {
                                async = 1;
                                postParm = ServiceAFweb.replaceAll("}}]}", "}}", postParm);
                            }
                        }

//                        dataSt = dataObj.getData();
//                        dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
//                        if (dataSt.indexOf("asynchronousRequest") == -1) {
//                            async = 1;
//                            int beg = dataSt.indexOf("{");
//                            if (beg != -1) {
//                                postParm = dataSt.substring(beg, dataSt.length() - 1);
//                            }
//                        } else {
//                            int beg = dataSt.indexOf("{");
//                            if (beg != -1) {
//                                postParm = dataSt.substring(beg);
//                                postParm += "]";
//                            }
//                        }
                    }
                }
                cmd.add("get wifi device"); // description
                cmd.add(WI_GetDevice);   // cmd
                cmd.add("get wifi devicestatus");
                cmd.add(WI_GetDeviceStatus);
                cmd.add("get device (hdml) "); // description
                cmd.add(WI_GetDeviceHDML);   // cmd                               
                pData.setCmd(cmd);
            } else if (oper.equals(WI_config)) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 3) {
                    banid = operList[0];
                    uniquid = operList[1];
                    prodClass = operList[2];
                    serialid = operList[3];
                    parm = operList[4];

                    if (operList.length > 5) {
                        dataSt = dataObj.getData();

                        int beg = dataSt.indexOf("{");
                        if (beg != -1) {
                            postParm = dataSt.substring(beg);
                            postParm += "}";
                            postParm = ServiceAFweb.replaceAll(":\",", ":\" \",", postParm);
                            postParm = ServiceAFweb.replaceAll("= ", "", postParm);
                            if (dataSt.indexOf("asynchronousRequest") != -1) {
                                async = 1;
                                postParm = ServiceAFweb.replaceAll("}}]}", "}}", postParm);
                            }
                        }
//                        dataSt = dataObj.getData();
//                        dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
//                        if (dataSt.indexOf("asynchronousRequest") != -1) {
//                            async = 1;
//                            int beg = dataSt.indexOf("{");
//                            if (beg != -1) {
//                                postParm = dataSt.substring(beg, dataSt.length() - 1);
//                            }
//                        } else {
//                            int beg = dataSt.indexOf("{");
//                            if (beg != -1) {
//                                postParm = dataSt.substring(beg);
//                                postParm += "}";
//                            }
//                        }
                    }
                }
                cmd.add("get wifi device"); // description
                cmd.add(WI_GetDevice);   // cmd
                cmd.add("get wifi devicestatus");
                cmd.add(WI_GetDeviceStatus);
                cmd.add("get device (hdml) "); // description
                cmd.add(WI_GetDeviceHDML);   // cmd                     
                pData.setCmd(cmd);
            } else if (oper.equals(WI_Callback)) {//"cancelAppointment")) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                // skip no information

            } else {
                logger.info("> getFeatureSsnsWifiProcess Other oper " + oper);
                return "";
            }
            if (oper.equals(WI_GetDevice)) {
                // for testing ignore WI_Getdev becase always no info
                return "";
                // for testing
            } else {
                if (serialid.equals("")) {
                    return "";
                }
            }
//            logger.info(dataSt);
/////////////
            //call devop to get customer id
            SsnsAcc NAccObj = new SsnsAcc();
            NAccObj.setDown("splunkflow");
            boolean stat = this.updateSsnsWifi(oper, banid, uniquid, prodClass, serialid, parm, postParm, pData, dataObj, NAccObj);
            if (stat == true) {
                if (async == 1) {
                    String feat = NAccObj.getName() + ":Async";
                    NAccObj.setName(feat);
                }
                boolean exist = false;
                String key = NAccObj.getName()
                        + NAccObj.getCusid()
                        + NAccObj.getBanid()
                        + NAccObj.getTiid();
                key = key.replaceAll(NAccObj.getUid(), "");
                ArrayList<SsnsAcc> ssnsAccObjList = getSsnsDataImp().getSsnsAccObjList(NAccObj.getName(), NAccObj.getUid());
                if (set.add(key)) {
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() != 0) {
                            SsnsAcc ssnsObj = ssnsAccObjList.get(0);
                            if (ssnsObj.getDown().equals("splunkflow")) {
                                exist = true;
                            }
                        }
                    }
                }

                if (exist == false) {

                    ssnsAccObjList = getSsnsDataImp().getSsnsAccObjListByTiid(NAccObj.getName(), NAccObj.getTiid());
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() > 3) {
                            exist = true;
                        }
                    }
                }
                if (exist == false) {
                    int ret = getSsnsDataImp().insertSsnsAccObject(NAccObj);
                }
            }
            return NAccObj.getName();
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsWifiProcess Exception " + ex.getMessage());
        }
        return "";
    }

    public boolean updateSsnsWifi(String oper, String banid, String uniquid, String prodClass, String serialid, String parm, String postParm, ProductData pData, SsnsData dataObj, SsnsAcc NAccObj) {
        try {
            String featTTV = "";
            int connectDevice = 0;
            int boost = 0;
            int extender = 0;
            if (oper.equals(WI_GetDeviceStatus) || oper.equals(WI_Callback) || oper.equals(WI_config)) {
                if ((banid.length() == 0) && (serialid.length() == 0)) {
                    return false;
                } else {
                    String outputSt = null;
                    if (oper.equals(WI_GetDeviceStatus)) {
                        outputSt = SendSsnsWifi(ServiceAFweb.URL_PRODUCT_PR, oper, banid, uniquid, prodClass, serialid, "", null);
                        if (outputSt == null) {
                            return false;
                        }
                        if (parm.length() > 0) {
                            String outputStConnect = SendSsnsWifi(ServiceAFweb.URL_PRODUCT_PR, oper, banid, uniquid, prodClass, serialid, parm, null);
                            if (outputStConnect.indexOf("macAddressTxt") != -1) {
                                connectDevice = 1;
                            }

                        }

                    } else if (oper.equals(WI_config)) {
                        outputSt = SendSsnsWifi(ServiceAFweb.URL_PRODUCT_PR, WI_GetDeviceStatus, banid, uniquid, prodClass, serialid, "", null);
                        if (outputSt == null) {
                            return false;
                        }
                        String outputDeviceSt = SendSsnsWifi(ServiceAFweb.URL_PRODUCT_PR, WI_GetDevice, banid, uniquid, prodClass, serialid, "", null);
                        if (outputDeviceSt.indexOf("Boost Device") != -1) {
                            boost = 1;
                        }
                        if (outputDeviceSt.indexOf("WirelessExtender") != -1) {
                            extender = 1;
                        }
                    }
                    if (outputSt == null) {
                        return false;
                    }
                    if (outputSt.length() == 0) {
                        return false;
                    }
//                    if (outputSt.length() < 80) {  // or test 
//                        return false;
//                    }
                    if (outputSt.indexOf("responseCode:400500") != -1) {
                        return false;
                    }
                    featTTV = parseWifiFeature(outputSt, oper, prodClass);
                    if (featTTV == null) {
                        return false;
                    }
                    if (connectDevice == 1) {
                        featTTV += ":" + parm;
                    }
                    if (boost == 1) {
                        featTTV += ":BoostD";
                    }
                    if (extender == 1) {
                        featTTV += ":ExtenderD";
                    }

                }
            } else if (oper.equals(APP_CAN_APP)) {   //"cancelAppointment";
                featTTV = APP_FEAT_TYPE_APP;
                featTTV += ":" + oper;
//                featTTV += ":" + host;
//                if ((banid.length() == 0) && (cust.length() == 0)) {
//                    featTTV += ":ContactEng";
//                }
            } else {
                return false;
            }
            if (banid.length() >= 10) {
                featTTV += ":NotaBan";
            }
//            logger.info("> updateSsnsWifi feat " + featTTV);
/////////////TTV   
            if (NAccObj.getDown().equals("splunkflow")) {
                ArrayList<String> callback = new ArrayList();
                int faulureCall = getSsnsFlowTraceWifiCallback(dataObj, callback, postParm);
                if (faulureCall == 0) {
                    pData.setCallback(callback);
                }
                ArrayList<String> flow = new ArrayList();
                int faulure = getSsnsFlowTrace(dataObj, flow);
                if (flow == null) {
                    logger.info("> updateSsnsAppointment skip no flow");
                    return false;
                }
                pData.setFlow(flow);

                if (faulure == 1) {
                    featTTV += ":splunkfailed";
                }
            }
            logger.info("> updateSsnsWifi feat " + featTTV);
            pData.setPostParam(postParm);
            NAccObj.setName(featTTV);
            NAccObj.setBanid(banid);
//            NAccObj.setCusid(dataObj.getCusid());
            NAccObj.setCusid(serialid);  // try to find out if duplicate BAN with the same SerialID

            String deviceInfo = uniquid + ":" + prodClass + ":" + serialid + ":" + parm + ":end";
            NAccObj.setTiid(deviceInfo);

            NAccObj.setUid(dataObj.getUid());
            NAccObj.setApp(dataObj.getApp());
            NAccObj.setOper(oper);

//          NAccObj.setDown(""); // set by NAccObj
            NAccObj.setRet(dataObj.getRet());
            NAccObj.setExec(dataObj.getExec());

            String nameSt = new ObjectMapper().writeValueAsString(pData);
            NAccObj.setData(nameSt);

            NAccObj.setUpdatedatel(dataObj.getUpdatedatel());
            NAccObj.setUpdatedatedisplay(new java.sql.Date(dataObj.getUpdatedatel()));

            return true;
        } catch (Exception ex) {
            logger.info("> updateSsnsAppointment Exception " + ex.getMessage());
        }
        return false;
    }

    public static String parseWifiFeature(String outputSt, String oper, String prodClass) {

        if (outputSt == null) {
            return null;
        }

        int smartInit = 0;
        int catInit = 0;
        int cujoInit = 0;

        String smartSteering = "";
        int guestDevice = 0;
        String frequency = "";
        int cujoAgent = 0;

        ArrayList<String> outputList = ServiceAFweb.prettyPrintJSON(outputSt);
        for (int j = 0; j < outputList.size(); j++) {
            String inLine = outputList.get(j);
//            logger.info("" + inLine);

            if (inLine.indexOf("smartSteeringEnabledInd") != -1) {
                if (catInit == 1) {
                    continue;
                }
                catInit = 1;
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("smartSteeringEnabledInd:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                smartSteering = valueSt;

                continue;
            }
            if (inLine.indexOf("guestDevice") != -1) {
                if (smartInit == 1) {
                    continue;
                }
                smartInit = 1;
                guestDevice = 1;
                continue;
            }
            if (inLine.indexOf("wirelessRadioFrequencyTxt") != -1) {

                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("wirelessRadioFrequencyTxt:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                String freq = frequency;
                if (freq.length() == 0) {
                    freq += valueSt;
                } else {
                    freq += "_" + valueSt;
                }
                frequency = freq;
                continue;
            }
            if (inLine.indexOf("cujoAgentEnabledInd") != -1) {
                if (cujoInit == 1) {
                    continue;
                }
                cujoInit = 1;
                cujoAgent = 1;;
                continue;
            }
        }

        String featTTV = APP_FEAT_TYPE_WIFI;
        featTTV += ":" + oper;
        featTTV += ":" + prodClass;

        String sm = smartSteering;
        if (sm.length() == 0) {
            sm = "noSSteering";
        } else {
            sm = "SSteering_" + sm;
        }
        featTTV += ":" + sm;

        String freq = frequency;
        if (freq.length() == 0) {
            freq = "noFreq";
        } else {
            freq = "Freq_" + freq;
        }
        featTTV += ":" + freq;

        String guest = "noGuestD";
        if (guestDevice == 1) {
            guest = "GuestD";
        }
        featTTV += ":" + guest;
        String cujo = "noCujo";
        if (cujoAgent == 1) {
            cujo = "Cujo";
        }
        featTTV += ":" + cujo;
        return featTTV;
    }

    // 1 faulure, 0 = success
    public int getSsnsFlowTraceWifiCallback(SsnsData dataObj, ArrayList<String> flow, String postParm) {
        if (postParm == null) {
            return 1;
        }
        if (postParm.length() == 1) {
            return 1;
        }
        String newUid = "";

        if (postParm.indexOf("asynchronousRequest") != -1) {
            String[] operList = postParm.split(",");
            for (int j = 0; j < operList.length; j++) {
                String inLine = operList[j];
                if (inLine.indexOf("operationId") != -1) {
                    String valueSt = inLine;
                    valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                    valueSt = ServiceAFweb.replaceAll("operationId:", "", valueSt);
                    newUid = valueSt;
                    break;
                }
            }
        }

        if (newUid.length() == 0) {
            return 1;
        }

        String uid = newUid;

        ArrayList<SsnsData> ssnsList = getSsnsDataImp().getSsnsDataObjListByUid(dataObj.getApp(), uid);
        if (ssnsList != null) {
//            logger.info("> ssnsList " + ssnsList.size());
            for (int i = 0; i < ssnsList.size(); i++) {
                SsnsData data = ssnsList.get(i);
                String flowSt = data.getDown();
                if (flowSt.length() == 0) {
                    flowSt = data.getOper();
                }
                flowSt += ":" + data.getExec();
                String dataTxt = data.getData();
                if (dataTxt.indexOf("[tocpresp,") != -1) {
                    try {
                        String valueSt = ServiceAFweb.replaceAll("[tocpresp,{node:", "", dataTxt);
                        valueSt = valueSt.substring(0, valueSt.length() - 2);
                        String filteredStr = valueSt.replaceAll(" ", "");
                        String[] filteredList = filteredStr.split("><");

                        flow.add(postParm);
                        for (int k = 0; k < filteredList.length; k++) {
                            String ln = filteredList[k];
                            if (k == 0) {
                                ln = ln + ">";
                            } else if (k == filteredList.length - 1) {
                                ln = "<" + ln;
                            } else {
                                ln = "<" + ln + ">";
                            }
                            flow.add(ln);
                        }
                        return 0;

                    } catch (Exception ex) {
                        logger.info(ex.getMessage());
                    }
                }

            }
        }
        return 1;

//        if (dataObj.getOper().equals(WI_config)) {
//            String dataSt = dataObj.getData();
//            dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
//            dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
//            dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
//            dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
//            dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
//            String[] dataList = dataSt.split(",");
//            String callUid = "";
//            for (int i = 0; i < dataList.length; i++) {
//                String inLine = dataList[i];
//                if (inLine.indexOf("operationId") != -1) {
//                    String valueSt = inLine;
//                    valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
//                    valueSt = ServiceAFweb.replaceAll("operationId:", "", valueSt);
//                    if (valueSt.length() >= 36) {
//                        callUid = valueSt.substring(0, 36);  // overrid uuid for call back
//                        break;
//                    }
//                }
//            }
//            if (callUid.length() > 0) {
//                ssnsList = getSsnsDataImp().getSsnsDataObjListByUid(dataObj.getApp(), callUid);
//                if (ssnsList != null) {
//                    for (int i = 0; i < ssnsList.size(); i++) {
//                        SsnsData data = ssnsList.get(i);
//                        String flowSt = data.getDown();
//                        if (flowSt.length() == 0) {
//                            flowSt = data.getOper();
//                        }
//                        flowSt += ":" + data.getExec();
//                        flowSt += ":" + data.getData();
//                        flow.add(flowSt);
//                    }
//                }
//            }
    }

    public String SendSsnsTestURL(String ProductURL, ArrayList<String> inList) {
        logger.info("> SendSsnsQual " + ProductURL);
        String url = ProductURL;
        try {
            if (inList != null) {
                inList.add(url);
            }
            // calculate elapsed time in milli seconds
            long startTime = TimeConvertion.currentTimeMillis();

            String output = this.sendRequest_Ssns(METHOD_GET, url, null, null, null);

            long endTime = TimeConvertion.currentTimeMillis();
            long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
            if (inList != null) {
                String tzid = "America/New_York"; //EDT
                TimeZone tz = TimeZone.getTimeZone(tzid);
                Date d = new Date(startTime);
                // timezone symbol (z) included in the format pattern 
                DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                // format date in target timezone
                format.setTimeZone(tz);
                String ESTdate = format.format(d);

                inList.add(ESTdate + " elapsedTime:" + elapsedTime);
                inList.add("output:");
            }

            return output;
        } catch (Exception ex) {
            logger.info("> SsnsAppointment exception " + ex.getMessage());
        }
        return null;
    }

    public String SendSsnsWifi(String ProductURL, String oper, String banid, String uniquid, String prodClass, String serialid, String parm, ArrayList<String> inList) {
        logger.info("> SendSsnsWifi " + oper + " " + banid + " " + uniquid + " " + prodClass + " " + serialid);

        String url = "";
        if (banid.length() >= 10) {
            logger.info("> SendSsnsWifi Bandid is Phonenumber " + banid);
        }
        if (oper.equals(WI_GetDevice)) {
            url = ProductURL + "/v1/cmo/selfmgmt/wifimanagement/account/" + banid
                    + "/device";
        } else if (oper.equals(WI_GetDeviceHDML)) {
            url = ProductURL + "/v1/cmo/selfmgmt/wifimanagement/account/" + banid
                    + "/device?source=hdml";
        } else if (oper.equals(WI_GetDeviceStatus)) {

            url = ProductURL + "/v1/cmo/selfmgmt/wifimanagement/account/" + banid
                    + "/device/organizationuniqueid/" + uniquid
                    + "/productclass/" + prodClass
                    + "/serialnumber/" + serialid
                    + "/status";

            if (parm.indexOf("connectdevicelist") != -1) {
                url += "?fields=connectDeviceList&connectdevicelist.statuscd=pause";
            }

        } else {
            return "";
        }
        try {
            if (inList != null) {
                inList.add(url);
            }
            // calculate elapsed time in milli seconds
            long startTime = TimeConvertion.currentTimeMillis();

            String output = this.sendRequest_Ssns(METHOD_GET, url, null, null, null);

            long endTime = TimeConvertion.currentTimeMillis();
            long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
            if (inList != null) {
                String tzid = "America/New_York"; //EDT
                TimeZone tz = TimeZone.getTimeZone(tzid);
                Date d = new Date(startTime);
                // timezone symbol (z) included in the format pattern 
                DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                // format date in target timezone
                format.setTimeZone(tz);
                String ESTdate = format.format(d);

                inList.add(ESTdate + " elapsedTime:" + elapsedTime);
                inList.add("output:");
            }

            return output;
        } catch (Exception ex) {
            logger.info("> SsnsAppointment exception " + ex.getMessage());
        }
        return null;
    }

    public String TestFeatureSsnsProdWifi(SsnsAcc dataObj, ArrayList<String> outputList, String Oper, String LABURL) {
        if (dataObj == null) {
            return "";
        }
        if (LABURL.length() == 0) {
            LABURL = ServiceAFweb.URL_PRODUCT_PR;
        }
        dataObj.getData();
        String banid = dataObj.getBanid();
        String appTId = dataObj.getTiid();
        if (appTId.length() == 0) {
            return "";
        }
        String WifiparL[] = appTId.split(":");

        String uniquid = WifiparL[0];
        String prodClass = WifiparL[1];
        String serialid = WifiparL[2];
        String parm = "";

        if (WifiparL.length > 3) {
            parm = WifiparL[3];
        }

        String outputSt = null;
        int connectDevice = 0;
        ArrayList<String> inList = new ArrayList();
        if (Oper.equals(WI_GetDeviceStatus)) {
            outputSt = SendSsnsWifi(LABURL, Oper, banid, uniquid, prodClass, serialid, "", inList);
            if (parm.length() > 0) {
                String outputStConnect = SendSsnsWifi(LABURL, Oper, banid, uniquid, prodClass, serialid, parm, null);
                if (outputStConnect.indexOf("macAddressTxt") != -1) {
                    connectDevice = 1;
                }

            }
            if (outputSt == null) {

                return "";
            }
            ////special char #, need to ignore for this system
            outputSt = outputSt.replaceAll("#", "");
            outputSt = outputSt.replaceAll("~", "");
            outputSt = outputSt.replaceAll("^", "");

            ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);
            String feat = parseWifiFeature(outputSt, Oper, prodClass);
            if (outputSt.indexOf("responseCode:400500") != -1) {
                feat += ":testfailed";
            }
            if (connectDevice == 1) {
                feat += ":" + parm;
            }
            outputList.add(feat);
            outputList.addAll(inList);
            outputList.addAll(outList);

            return feat;
        } else if (Oper.equals(WI_GetDevice) || Oper.equals(WI_GetDeviceHDML)) {

            outputSt = SendSsnsWifi(LABURL, Oper, banid, uniquid, prodClass, serialid, Oper, inList);
            if (outputSt == null) {
                return "";
            }
            ////special char #, need to ignore for this system
            outputSt = outputSt.replaceAll("#", "");
            outputSt = outputSt.replaceAll("~", "");
            outputSt = outputSt.replaceAll("^", "");

            ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);

            String feat = dataObj.getName();
            for (int i = 0; i < outList.size(); i++) {
                String inLine = outList.get(i);
                inLine = ServiceAFweb.replaceAll("\"", "", inLine);
                inLine = ServiceAFweb.replaceAll(",", "", inLine);

                if (inLine.indexOf("deviceTypeCd") != -1) {
                    String dCd = ServiceAFweb.replaceAll("deviceTypeCd:", "", inLine);
                    feat += ":" + dCd;
                }
                if (inLine.indexOf("productClassId") != -1) {
                    String dCd = ServiceAFweb.replaceAll("productClassId:", "", inLine);
                    feat += ":" + dCd;
                }
            }
            if (outputSt.indexOf("responseCode:400500") != -1) {
                feat += ":testfailed";
            }
            outputList.add(feat);
            outputList.addAll(inList);
            outputList.addAll(outList);
            return feat;
        }

        return "";
    }

////////////////////////////////////////////    
    public String getFeatureSsnsAppointment(SsnsData dataObj) {
        String feat = "";
        try {
            feat = getFeatureSsnsAppointmentProcess(dataObj);
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsAppointment Exception " + ex.getMessage());
        }
        getSsnsDataImp().updatSsnsDataStatusById(dataObj.getId(), ConstantKey.COMPLETED);
        return feat;
    }

    public String getFeatureSsnsAppointmentProcess(SsnsData dataObj) {
        ProductData pData = new ProductData();
        ArrayList<String> cmd = new ArrayList();
        if (dataObj == null) {
            return "";
        }

        String appTId = "";
        String banid = "";
        String cust = "";
        String host = "";
        String dataSt = "";
        String postParm = "";
        int devOPflag = 0;
        try {
            String oper = dataObj.getOper();
            if (oper.equals(APP_UPDATE)) { //"updateAppointment")) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 3) {
                    appTId = operList[0];
                    banid = operList[1];
                    banid = banid.replace("ban:", "");
                    cust = operList[2];
                    cust = cust.replace("customerId:", "");

                    for (int k = 0; k < operList.length; k++) {
                        String inLine = operList[k];

                        if (inLine.indexOf("hostSystemCd:") != -1) {
                            host = inLine;
                            host = host.replace("hostSystemCd:", "");
                        }
                    }
                    if (operList.length > 5) {
                        dataSt = dataObj.getData();

                        int beg = dataSt.indexOf("{");
                        if (beg != -1) {
                            postParm = dataSt.substring(beg);
                            postParm += "}";
                            postParm = ServiceAFweb.replaceAll(":\",", ":\" \",", postParm);
                            postParm = ServiceAFweb.replaceAll("= ", "", postParm);
                            postParm = ServiceAFweb.replaceAll("}]}", "}", postParm);
                        }
                    }

                }
                if ((banid.length() == 0) && (cust.length() == 0)) {
                    ;
                } else {
                    cmd.add("get appointment"); // cmd
                    cmd.add(APP_GET_APP);  // descriptoin
                }
                cmd.add("search timeslot");
                cmd.add(APP_GET_TIMES);
                pData.setCmd(cmd);

            } else if (oper.equals(APP_GET_TIMES)) { //"timeslot")) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 3) {
                    int custInti = 0;
                    for (int k = 0; k < operList.length; k++) {
                        String inLine = operList[k];
                        if (inLine.indexOf("ban:") != -1) {
                            banid = inLine;
                            banid = host.replace("ban:", "");
                            continue;
                        }
                        if (inLine.indexOf("customerId:") != -1) {

                            cust = inLine;
                            cust = host.replace("customerId:", "");
                            continue;
                        }
                        if (inLine.indexOf("id:") != -1) {
                            if (custInti == 1) {
                                continue;
                            }
                            custInti = 1;
                            appTId = inLine;
                            appTId = appTId.replace("id:", "");
                            continue;
                        }
                        if (inLine.indexOf("hostSystemCd:") != -1) {
                            host = inLine;
                            host = host.replace("hostSystemCd:", "");
                            continue;
                        }
                    }
                    if ((banid.length() == 0) && (cust.length() == 0)) {
                        ;
                    } else {
                        cmd.add("get appointment");
                        cmd.add(APP_GET_APP);
                    }
                    cmd.add("search timeslot");
                    cmd.add(APP_GET_TIMES);
                    pData.setCmd(cmd);
                }
            } else if (oper.equals(APP_GET_APP)) { //"getAppointment")) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 3) {
                    banid = operList[0];
                    cust = operList[1];
                }
                cmd.add("get appointment");
                cmd.add(APP_GET_APP);
                pData.setCmd(cmd);
            } else if (oper.equals(APP_CAN_APP)) {//"cancelAppointment")) {
                dataSt = dataObj.getData();
                dataSt = ServiceAFweb.replaceAll("\"", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("[", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("]", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("{", "", dataSt);
                dataSt = ServiceAFweb.replaceAll("}", "", dataSt);
                String[] operList = dataSt.split(",");
                if (operList.length > 3) {
                    appTId = operList[0];
                    banid = operList[1];
                    if (banid.equals("null")) {
                        banid = "";
                    }
                    cust = operList[2];
                    if (cust.equals("null")) {
                        cust = "";
                    }
                    host = operList[3];
                }
            } else {
                logger.info("> getFeatureSsnsAppointmentProcess Other oper " + oper);
            }
            if (oper.equals(APP_GET_APP)) {
                // for testing ignore APP_GET_APP becase alwasy no info
                return "";
                // for testing
            } else {
                if (appTId.equals("")) {
                    return "";
                }
            }
//            logger.info(dataSt);
/////////////
            //call devop to get customer id
//            if ((banid.length() == 0) && (cust.length() == 0)) {
//                if (CKey.DEVOP == true) {
//                    if (host.equals("FIFA") || host.equals("LYNX")) {
//                        String custid = getCustIdAppointmentDevop(ServiceAFweb.URL_PRODUCT_PR, appTId, banid, cust, host);
//                        if (custid.length() != 0) {
//                            cust = custid;
//                            dataObj.setCusid(custid);
//                            devOPflag = 1;
//                            logger.info("> getFeatureSsnsAppointmentProcess found Ticket to custid " + cust);
//                        }
//                    }
//                }
//            }
            SsnsAcc NAccObj = new SsnsAcc();
            NAccObj.setDown("splunkflow");

            boolean stat = this.updateSsnsAppointment(oper, appTId, banid, cust, host, postParm, pData, dataObj, NAccObj);
            if (stat == true) {
//                if (devOPflag == 1) {
//                    String feat = NAccObj.getName() + ":TicktoCust";
//                    NAccObj.setName(feat);
//                }
                boolean exist = false;
                String key = NAccObj.getName()
                        + NAccObj.getCusid()
                        + NAccObj.getBanid()
                        + NAccObj.getTiid();
                key = key.replaceAll(NAccObj.getUid(), "");
                ArrayList<SsnsAcc> ssnsAccObjList = getSsnsDataImp().getSsnsAccObjList(NAccObj.getName(), NAccObj.getUid());
                if (set.add(key)) {
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() != 0) {
                            SsnsAcc ssnsObj = ssnsAccObjList.get(0);
                            if (ssnsObj.getDown().equals("splunkflow")) {
                                exist = true;
                            }
                        }
                    }
                }

                if (exist == false) {
                    ssnsAccObjList = getSsnsDataImp().getSsnsAccObjListByTiid(NAccObj.getName(), NAccObj.getTiid());
                    if (ssnsAccObjList != null) {
                        if (ssnsAccObjList.size() > 3) {
                            exist = true;
                        }
                    }
                }
                if (exist == false) {
                    int ret = getSsnsDataImp().insertSsnsAccObject(NAccObj);
                }

            }
            return NAccObj.getName();
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsAppointmentProcess Exception " + ex.getMessage());
        }
        return "";
    }

    public boolean updateSsnsAppointment(String oper, String appTId, String banid, String cust, String host, String postParm, ProductData pData, SsnsData dataObj, SsnsAcc NAccObj) {
        try {
            String featTTV = "";
            String outputSt = null;
            if (oper.equals(APP_UPDATE) || oper.equals(APP_GET_TIMES) || oper.equals(APP_GET_APP)) {
                if ((banid.length() == 0) && (cust.length() == 0)) {

                    outputSt = SendSsnsAppointmentGetTimeslot(ServiceAFweb.URL_PRODUCT_PR, appTId, banid, cust, host, null);
                    if (outputSt == null) {
                        return false;
                    }
                    if (outputSt.length() < 80) {
                        // special case for no appointment {"status":{"statusCd":"200","statusTxt":"OK"},"appointmentList":[]}
                        return false;
                    }
                    if (outputSt.indexOf("responseCode:400500") != -1) {
                        return false;
                    }
                    featTTV = parseAppointmentTimeSlotFeature(outputSt, oper, host);

                } else {
                    if (oper.equals(APP_GET_TIMES)) {
                        outputSt = SendSsnsAppointmentGetTimeslot(ServiceAFweb.URL_PRODUCT_PR, appTId, banid, cust, host, null);
                        if (outputSt == null) {
                            return false;
                        }
                        if (outputSt.length() < 80) {
                            // special case for no appointment {"status":{"statusCd":"200","statusTxt":"OK"},"appointmentList":[]}
                            return false;
                        }
                        if (outputSt.indexOf("responseCode:400500") != -1) {
                            return false;
                        }
                        featTTV = parseAppointmentTimeSlotFeature(outputSt, oper, host);
                    } else {
                        outputSt = SendSsnsAppointmentGetApp(ServiceAFweb.URL_PRODUCT_PR, appTId, banid, cust, host, null);
                        if (outputSt == null) {
                            return false;
                        }
                        if (outputSt.length() < 80) {
                            // special case for no appointment {"status":{"statusCd":"200","statusTxt":"OK"},"appointmentList":[]}
                            return false;
                        }
                        if (outputSt.indexOf("responseCode:400500") != -1) {
                            return false;
                        }
                        featTTV = parseAppointmentFeature(outputSt, oper);
                    }

                }

            } else if (oper.equals(APP_CAN_APP)) {   //"cancelAppointment";
                featTTV = APP_FEAT_TYPE_APP;
                featTTV += ":" + oper;
                featTTV += ":" + host;
                if ((banid.length() == 0) && (cust.length() == 0)) {
                    featTTV += ":ContactEng";
                } else {
                    featTTV += ":TD";
                }
            } else {
                return false;
            }

//            logger.info("> updateSsnsAppointment feat " + featTTV);
/////////////TTV   
            if (NAccObj.getDown().equals("splunkflow")) {

                ArrayList<String> flow = new ArrayList();
                int faulure = getSsnsFlowTrace(dataObj, flow);
                if (flow == null) {
                    logger.info("> updateSsnsAppointment skip no flow");
                    return false;
                }
                pData.setFlow(flow);

                if (faulure == 1) {
                    featTTV += ":splunkfailed";
                }
            }

            logger.info("> updateSsnsAppointment feat " + featTTV);
            pData.setPostParam(postParm);
            NAccObj.setName(featTTV);
            NAccObj.setBanid(banid);
            NAccObj.setCusid(cust);
            NAccObj.setTiid(appTId);
            NAccObj.setUid(dataObj.getUid());
            NAccObj.setApp(dataObj.getApp());
            NAccObj.setOper(oper);

//          NAccObj.setDown(""); // set by NAccObj
            NAccObj.setRet(host);
            NAccObj.setExec(dataObj.getExec());

            String nameSt = new ObjectMapper().writeValueAsString(pData);
            NAccObj.setData(nameSt);

            NAccObj.setUpdatedatel(dataObj.getUpdatedatel());
            NAccObj.setUpdatedatedisplay(new java.sql.Date(dataObj.getUpdatedatel()));

            return true;
        } catch (Exception ex) {
            logger.info("> updateSsnsAppointment Exception " + ex.getMessage());
        }
        return false;
    }

    public static String parseAppointmentTimeSlotFeature(String outputSt, String oper, String host) {

        if (outputSt == null) {
            return "";
        }
        ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);
        String feat = APP_FEAT_TYPE_APP;
        feat += ":" + oper;
        feat += ":" + host;
        feat += ":ticketID";

        int NumofStart = 0;
        for (int i = 0; i < outList.size(); i++) {
            String inLine = outList.get(i);
            inLine = ServiceAFweb.replaceAll("\"", "", inLine);
            if (inLine.indexOf("startDate") != -1) {
                NumofStart++;
            }
        }
        if (NumofStart > 0) {
            feat += ":startdate";
        } else {
            feat += ":nostartdate";
        }

        return feat;
    }

    public static String parseAppointmentFeature(String outputSt, String oper) {

        if (outputSt == null) {
            return "";
        }

        int catCdInit = 0;
        int statInit = 0;
        int catInit = 0;
        int hostInit = 0;
        String category = "";
        String statusCd = "";
        String categoryCd = "";
        String host = "";

        ArrayList<String> outputList = ServiceAFweb.prettyPrintJSON(outputSt);
        for (int j = 0; j < outputList.size(); j++) {
            String inLine = outputList.get(j);
//            logger.info("" + inLine);

            if (inLine.indexOf("category") != -1) {
                if (catInit == 1) {
                    continue;
                }
                catInit = 1;
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("category:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                category = valueSt;

                continue;
            }
            if (inLine.indexOf("statusCd") != -1) {
                if (catInit == 0) {
                    continue;
                }
                if (statInit == 1) {
                    continue;
                }

                statInit = 1;
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("statusCd:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                statusCd = valueSt;
                continue;
            }
            if (inLine.indexOf("productCategoryCd") != -1) {

                if (catCdInit == 1) {
                    continue;
                }
                catCdInit = 1;
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("productCategoryCd:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                categoryCd = valueSt;
                continue;
            }
            if (inLine.indexOf("hostSystemCd") != -1) {

                if (hostInit == 1) {
                    continue;
                }
                hostInit = 1;
                String valueSt = inLine;
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("hostSystemCd:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(",", "", valueSt);
                host = valueSt;
                continue;
            }
        }

        String featTTV = APP_FEAT_TYPE_APP;
        featTTV += ":" + oper;
        featTTV += ":" + host;
        featTTV += ":" + category;
        featTTV += ":" + statusCd;
        featTTV += ":" + categoryCd;

        return featTTV;
    }

//    public String getCustIdAppointmentDevop(String ProductURL, String appTId, String banid, String cust, String host) {
//        String url = "http://localhost:8080/v2/cmo/selfmgmt/appointmentmanagement/devop/searchtimeslot";
//        HashMap newbodymap = new HashMap();
//        newbodymap.put("customerId", cust);
//        newbodymap.put("id", appTId);
//        newbodymap.put("hostSystemCd", host);
//        try {
//            String custid = "";
//            String output = this.sendRequest_Ssns(METHOD_POST, url, null, newbodymap, null);
//
//            if (output == null) {
//                return "";
//            }
//            if (output.indexOf("responseCode:400500") != -1) {
//                return "";
//
//            }
//            ArrayList arrayItem = new ObjectMapper().readValue(output, ArrayList.class
//            );
//            if (arrayItem.size() < 1) {
//                return "";
//            }
//            output = (String) arrayItem.get(1);
//            output = ServiceAFweb.replaceAll("\"", "", output);
//            output = ServiceAFweb.replaceAll("\\", "", output);
//            String[] oList = output.split(",");
//            for (int i = 0; i < oList.length; i++) {
//                String line = oList[i];
//                if (line.indexOf("customerId:") != -1) {
//                    custid = ServiceAFweb.replaceAll("customerId:", "", line);
//                    if (custid.equals("null")) {
//                        return "";
//                    }
//                    return custid;
//                }
//            }
//
//            return "";
//        } catch (Exception ex) {
////            logger.info("> getCustIdAppointmentDevop exception " + ex.getMessage());
//        }
//        return "";
//    }
    public String SendSsnsAppointmentGetTimeslot(String ProductURL, String appTId, String banid, String cust, String host, ArrayList<String> inList) {
        logger.info("> SendSsnsAppointmentGetApp " + appTId + " " + banid + " " + cust);
        String url = ProductURL + "/v2/cmo/selfmgmt/appointmentmanagement/searchtimeslot";

        HashMap newbodymap = new HashMap();
        if (cust.length() > 0) {
            newbodymap.put("customerId", cust);
        }
        newbodymap.put("id", appTId);
        newbodymap.put("hostSystemCd", host);
        try {
            if (inList != null) {
                inList.add(url);
            }
            // calculate elapsed time in milli seconds
            long startTime = TimeConvertion.currentTimeMillis();

            String output = this.sendRequest_Ssns(METHOD_POST, url, null, newbodymap, null);

            long endTime = TimeConvertion.currentTimeMillis();
            long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
            if (inList != null) {
                String tzid = "America/New_York"; //EDT
                TimeZone tz = TimeZone.getTimeZone(tzid);
                Date d = new Date(startTime);
                // timezone symbol (z) included in the format pattern 
                DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                // format date in target timezone
                format.setTimeZone(tz);
                String ESTdate = format.format(d);

                inList.add(ESTdate + " elapsedTime:" + elapsedTime);
                String bodyElement = new ObjectMapper().writeValueAsString(newbodymap);
                inList.add("bodyElement:" + bodyElement);
                inList.add("output:");
            }

            return output;
        } catch (Exception ex) {
            logger.info("> SsnsAppointment exception " + ex.getMessage());
        }
        return null;
    }

    public String TestFeatureSsnsProdApp(SsnsAcc dataObj, ArrayList<String> outputList, String Oper, String LABURL) {
        if (dataObj == null) {
            return "";
        }
        if (LABURL.length() == 0) {
            LABURL = ServiceAFweb.URL_PRODUCT_PR;
        }
        dataObj.getData();
        String banid = dataObj.getBanid();
        String appTId = dataObj.getTiid();
        String cust = dataObj.getCusid();
        String host = dataObj.getRet();
        String outputSt = null;
        ArrayList<String> inList = new ArrayList();
        if (Oper.equals(APP_GET_APP)) {

            outputSt = SendSsnsAppointmentGetApp(LABURL, appTId, banid, cust, host, inList);
            if (outputSt == null) {
                return "";
            }
            ////special char #, need to ignore for this system
            outputSt = outputSt.replaceAll("#", "");
            outputSt = outputSt.replaceAll("~", "");
            outputSt = outputSt.replaceAll("^", "");

            ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);
            String feat = parseAppointmentFeature(outputSt, Oper);
            if (outputSt.indexOf("responseCode:400500") != -1) {
                feat += ":testfailed";
            }
            outputList.add(feat);
            outputList.addAll(inList);
            outputList.addAll(outList);

            return feat;
        } else if (Oper.equals(APP_GET_TIMES)) {
            outputSt = SendSsnsAppointmentGetTimeslot(LABURL, appTId, banid, cust, host, inList);
            if (outputSt == null) {
                return "";
            }
            ////special char #, need to ignore for this system
            outputSt = outputSt.replaceAll("#", "");
            outputSt = outputSt.replaceAll("~", "");
            outputSt = outputSt.replaceAll("^", "");

            ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);
            String feat = parseAppointmentTimeSlotFeature(outputSt, Oper, host);

            if (outputSt.indexOf("responseCode:400500") != -1) {
                feat += ":testfailed";
            }
            outputList.add(feat);
            outputList.addAll(inList);
            outputList.addAll(outList);
            return feat;
        }

        return "";
    }

    public String SendSsnsAppointmentGetApp(String ProductURL, String appTId, String banid, String cust, String host, ArrayList<String> inList) {
        logger.info("> SendSsnsAppointmentGetApp " + appTId + " " + banid + " " + cust);

        if (host.length() > 0) {
            host = host.replace("9", ""); // remove OMS9
            host = host.replace("6", ""); // remove OMS9
        }
        String url = ProductURL + "/v2/cmo/selfmgmt/appointmentmanagement/appointment?customerid=" + cust;
        if (banid.length() > 0) {
            url = ProductURL + "/v2/cmo/selfmgmt/appointmentmanagement/appointment?ban=" + banid + "&customerid=" + cust;
            if (host.length() > 0) {
                url += "&appointmentlist.hostsystemcd.in=" + host;
            }
        }
        try {
//            logger.info("> SendSsnsAppointmentGetApp url: " + url + ", host: " + host);
            if (inList != null) {
                inList.add(url);
            }
            // calculate elapsed time in milli seconds
            long startTime = TimeConvertion.currentTimeMillis();

            String output = this.sendRequest_Ssns(METHOD_GET, url, null, null, null);

            long endTime = TimeConvertion.currentTimeMillis();
            long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
            if (inList != null) {
                String tzid = "America/New_York"; //EDT
                TimeZone tz = TimeZone.getTimeZone(tzid);
                Date d = new Date(startTime);
                // timezone symbol (z) included in the format pattern 
                DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                // format date in target timezone
                format.setTimeZone(tz);
                String ESTdate = format.format(d);

                inList.add(ESTdate + " elapsedTime:" + elapsedTime);
                inList.add("output:");
            }
            return output;
        } catch (Exception ex) {
            logger.info("> SsnsAppointment exception " + ex.getMessage());
        }
        return null;
    }

    public String TestFeatureSsnsProductInventory(SsnsAcc dataObj, ArrayList<String> outputList, String oper, String LABURL) {
        if (dataObj == null) {
            return "";
        }
        if (LABURL.length() == 0) {
            LABURL = ServiceAFweb.URL_PRODUCT_PR;
        }
        String banid = dataObj.getBanid();
        String prodid = dataObj.getTiid();
        ArrayList<String> inList = new ArrayList();
        String outputSt = SendSsnsProdiuctInventory(LABURL, banid, prodid, oper, inList);
        if (outputSt == null) {
            return "";
        }
        String featTTV = "";
        ////special char #, need to ignore for this system
        outputSt = outputSt.replaceAll("#", "");
        outputSt = outputSt.replaceAll("~", "");
        outputSt = outputSt.replaceAll("^", "");
        ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);
        if (oper.equals(APP_FEAT_TYPE_HSIC)) {
            featTTV = parseProductInternetFeature(outputSt, dataObj.getOper());

        } else if (oper.equals(APP_FEAT_TYPE_TTV)) {
            featTTV = parseProductTtvFeature(outputSt, dataObj.getOper());

        } else if (oper.equals(APP_FEATT_TYPE_SING)) {
            featTTV = parseProductPhoneFeature(outputSt, dataObj.getOper(), null);

        }
        if (outputSt.indexOf("responseCode:400500") != -1) {
            featTTV += ":testfailed";
        }
        outputList.add(featTTV);
        outputList.addAll(inList);
        outputList.addAll(outList);

        return featTTV;
    }

    public String TestFeatureSsnsActCfg(SsnsAcc dataObj, ArrayList<String> outputList, String oper, String LABURL) {
        if (dataObj == null) {
            return "";
        }
        if (LABURL.length() == 0) {
            LABURL = ServiceAFweb.URL_PRODUCT_PR;
        }

        String custid = dataObj.getCusid();
        String consumer = "C";
        String service = dataObj.getRet();

        ArrayList<String> inList = new ArrayList();
        String outputSt = SendSsnsActCfg(LABURL, custid, consumer, service, inList);
        if (outputSt == null) {
            return "";
        }
        String featTTV = "";
        ////special char #, need to ignore for this system
        outputSt = outputSt.replaceAll("#", "");
        outputSt = outputSt.replaceAll("~", "");
        outputSt = outputSt.replaceAll("^", "");
        ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);

        featTTV = parseActCfgFeature(outputSt, oper, service);

        if (outputSt.indexOf("responseCode:400500") != -1) {
            featTTV += ":testfailed";
        }
        outputList.add(featTTV);
        outputList.addAll(inList);
        outputList.addAll(outList);

        return featTTV;
    }

    public String TestFeatureSsnsCallControl(SsnsAcc dataObj, ArrayList<String> outputList, String oper, String LABURL) {
        if (dataObj == null) {
            return "";
        }
        if (LABURL.length() == 0) {
            LABURL = ServiceAFweb.URL_PRODUCT_PR;
        }
        String banid = dataObj.getBanid();
        String phone = dataObj.getCusid();
        String host = dataObj.getRet();

        ArrayList<String> inList = new ArrayList();
        String outputSt = SendSsnsCallControl(LABURL, banid, phone, host, inList);
        if (outputSt == null) {
            return "";
        }
        String featTTV = "";
        ////special char #, need to ignore for this system
        outputSt = outputSt.replaceAll("#", "");
        outputSt = outputSt.replaceAll("~", "");
        outputSt = outputSt.replaceAll("^", "");
        ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);

        featTTV = parseCallControlFeature(outputSt, oper, host);

        if (outputSt.indexOf("responseCode:400500") != -1) {
            featTTV += ":testfailed";
        }
        outputList.add(featTTV);
        outputList.addAll(inList);
        outputList.addAll(outList);

        return featTTV;
    }

    public String TestFeatureSsnsCallControlFromProdInv(SsnsAcc dataObj, ArrayList<String> outputList, String oper, String LABURL) {
        if (dataObj == null) {
            return "";
        }
        if (LABURL.length() == 0) {
            LABURL = ServiceAFweb.URL_PRODUCT_PR;
        }
        String banid = dataObj.getBanid();

        String appTId = dataObj.getCusid();
        if (appTId.length() == 0) {
            return "";
        }
        String CCparL[] = appTId.split(":");

        String phone = CCparL[0];
        String host = CCparL[1];

        ArrayList<String> inList = new ArrayList();
        String outputSt = SendSsnsCallControl(LABURL, banid, phone, host, inList);
        if (outputSt == null) {
            return "";
        }
        String featTTV = "";
        ////special char #, need to ignore for this system
        outputSt = outputSt.replaceAll("#", "");
        outputSt = outputSt.replaceAll("~", "");
        outputSt = outputSt.replaceAll("^", "");
        ArrayList<String> outList = ServiceAFweb.prettyPrintJSON(outputSt);

        featTTV = parseCallControlFeature(outputSt, oper, host);

        if (outputSt.indexOf("responseCode:400500") != -1) {
            featTTV += ":testfailed";
        }
        outputList.add(featTTV);
        outputList.addAll(inList);
        outputList.addAll(outList);

        return featTTV;
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////    
    public String getFeatureSsnsProdiuctInventory(SsnsData dataObj) {
        String feat = "";
        try {
            feat = getFeatureSsnsProdiuctInventoryProcess(dataObj);
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsAppointment Exception " + ex.getMessage());
        }
        getSsnsDataImp().updatSsnsDataStatusById(dataObj.getId(), ConstantKey.COMPLETED);
        return feat;
    }

    public String getFeatureSsnsProdiuctInventoryProcess(SsnsData dataObj) {

        ProductData pData = new ProductData();
        ArrayList<String> cmd = new ArrayList();
        if (dataObj == null) {
            return "";
        }
        String prodid = "";
        String banid = "";
        try {

            String oper = dataObj.getOper();
            String daSt = dataObj.getData();
            //["xxx","xxx","product.characteristic.channelInfoList",null,null,null]
            daSt = ServiceAFweb.replaceAll("[", "", daSt);
            daSt = ServiceAFweb.replaceAll("]", "", daSt);
            daSt = ServiceAFweb.replaceAll("\"", "", daSt);
            String[] daList = daSt.split(",");
            if (daList.length < 3) {
                return "";
            }
            if (oper.equals(PROD_GET_BYID)) {
                prodid = daList[0];
                banid = daList[1];
            } else if (oper.equals(PROD_GET_PROD)) {
                banid = daList[0];
            } else {
                logger.info("> getFeatureSsnsProdiuctInventory Other oper " + oper);
            }
            if (banid.equals("null")) {
                return "";
            }

//            logger.info(daSt);
/////////////
            if (oper.equals(PROD_GET_BYID)) {

                String outputSt = SendSsnsProdiuctInventoryByProdId(ServiceAFweb.URL_PRODUCT_PR, banid, prodid, null);
                if (outputSt == null) {
                    return "";
                }
                ArrayList<String> outputList = ServiceAFweb.prettyPrintJSON(outputSt);
                String valueSt = "";
                for (int j = 0; j < outputList.size(); j++) {
                    String inLine = outputList.get(outputList.size() - 1 - j);
                    if (inLine.indexOf("productType") != -1) {
                        valueSt = inLine;
                        valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                        valueSt = ServiceAFweb.replaceAll("productType:", "", valueSt);
                        break;
                    }
                }
// 
                if (valueSt.length() == 0) {
                    return "";
                }
                String PIoper = valueSt;

                SsnsAcc NAccObj = new SsnsAcc();
                NAccObj.setTiid(prodid);
                NAccObj.setRet(PIoper);
                NAccObj.setDown("splunkflow");
//    public static String APP_PRODUCT_TYPE_TTV = "TTV";
//    public static String APP_PRODUCT_TYPE_HSIC = "HSIC";
//    public static String APP_PRODUCT_TYPE_SING = "SING";                
                cmd = new ArrayList();
                cmd.add("get " + PIoper + " productby id"); // description
                cmd.add(PROD_GET_BYID); // cmd
                pData.setCmd(cmd);

                boolean stat = this.updateSsnsProdiuctInventoryByProdId(PIoper, banid, prodid, pData, dataObj, NAccObj);
                if (stat == true) {

                    boolean exist = false;
                    String key = NAccObj.getName()
                            + NAccObj.getCusid()
                            + NAccObj.getBanid()
                            + NAccObj.getTiid();
                    key = key.replaceAll(NAccObj.getUid(), "");
                    ArrayList<SsnsAcc> ssnsAccObjList = getSsnsDataImp().getSsnsAccObjList(NAccObj.getName(), NAccObj.getUid());
                    if (set.add(key)) {
                        if (ssnsAccObjList != null) {
                            if (ssnsAccObjList.size() != 0) {
                                SsnsAcc ssnsObj = ssnsAccObjList.get(0);
                                if (ssnsObj.getDown().equals("splunkflow")) {
                                    exist = true;
                                }
                            }
                        }
                    }

                    if (exist == false) {
                        ssnsAccObjList = getSsnsDataImp().getSsnsAccObjListByBan(NAccObj.getName(), NAccObj.getBanid());
                        if (ssnsAccObjList != null) {
                            if (ssnsAccObjList.size() > 3) {
                                exist = true;
                            }
                        }
                    }
                    if (exist == false) {
                        int ret = getSsnsDataImp().insertSsnsAccObject(NAccObj);
                    }
                }
            }
//            
            SsnsAcc NAccObj = new SsnsAcc();
            if (oper.equals(PROD_GET_PROD)) {

                NAccObj.setTiid(prodid);
                NAccObj.setRet(APP_FEATT_TYPE_SING);
                NAccObj.setDown("splunkflow");
                String PIoper = APP_FEATT_TYPE_SING;
                cmd = new ArrayList();
                cmd.add("get " + PIoper + " productby id"); // description
                cmd.add(PROD_GET_BYID); // cmd
                pData.setCmd(cmd);

                boolean stat = this.updateSsnsProdiuctInventory(PIoper, banid, prodid, pData, dataObj, NAccObj);
                if (stat == true) {

                    boolean exist = false;
                    String key = NAccObj.getName()
                            + NAccObj.getCusid()
                            + NAccObj.getBanid()
                            + NAccObj.getTiid();
                    key = key.replaceAll(NAccObj.getUid(), "");
                    ArrayList<SsnsAcc> ssnsAccObjList = getSsnsDataImp().getSsnsAccObjList(NAccObj.getName(), NAccObj.getUid());
                    if (set.add(key)) {
                        if (ssnsAccObjList != null) {
                            if (ssnsAccObjList.size() != 0) {
                                SsnsAcc ssnsObj = ssnsAccObjList.get(0);
                                if (ssnsObj.getDown().equals("splunkflow")) {
                                    exist = true;
                                }
                            }
                        }
                    }
                    if (exist == false) {
                        ssnsAccObjList = getSsnsDataImp().getSsnsAccObjListByBan(NAccObj.getName(), NAccObj.getBanid());
                        if (ssnsAccObjList != null) {
                            if (ssnsAccObjList.size() > 3) {
                                exist = true;
                            }
                        }
                    }
                    if (exist == false) {
                        int ret = getSsnsDataImp().insertSsnsAccObject(NAccObj);
                    }

                }

                NAccObj = new SsnsAcc();
                NAccObj.setTiid(prodid);
                NAccObj.setRet(APP_FEAT_TYPE_HSIC);
                NAccObj.setDown("splunkflow");
                PIoper = APP_FEAT_TYPE_HSIC;
                cmd = new ArrayList();
                cmd.add("get " + PIoper + " productby id"); // description
                cmd.add(PROD_GET_BYID); // cmd
                pData.setCmd(cmd);

                stat = this.updateSsnsProdiuctInventory(PIoper, banid, prodid, pData, dataObj, NAccObj);
                if (stat == true) {
                    boolean exist = false;
                    String key = NAccObj.getName()
                            + NAccObj.getCusid()
                            + NAccObj.getBanid()
                            + NAccObj.getTiid();
                    key = key.replaceAll(NAccObj.getUid(), "");
                    ArrayList<SsnsAcc> ssnsAccObjList = getSsnsDataImp().getSsnsAccObjList(NAccObj.getName(), NAccObj.getUid());
                    if (set.add(key)) {
                        if (ssnsAccObjList != null) {
                            if (ssnsAccObjList.size() != 0) {
                                SsnsAcc ssnsObj = ssnsAccObjList.get(0);
                                if (ssnsObj.getDown().equals("splunkflow")) {
                                    exist = true;
                                }
                            }
                        }
                    }
                    if (exist == false) {
                        ssnsAccObjList = getSsnsDataImp().getSsnsAccObjListByBan(NAccObj.getName(), NAccObj.getBanid());
                        if (ssnsAccObjList != null) {
                            if (ssnsAccObjList.size() > 3) {
                                exist = true;
                            }
                        }
                    }
                    if (exist == false) {
                        int ret = getSsnsDataImp().insertSsnsAccObject(NAccObj);
                    }
                }

                NAccObj = new SsnsAcc();
                NAccObj.setTiid(prodid);
                NAccObj.setRet(APP_FEAT_TYPE_TTV);
                NAccObj.setDown("splunkflow");
                PIoper = APP_FEAT_TYPE_TTV;
                cmd = new ArrayList();
                cmd.add("get " + PIoper + " productby id"); // description
                cmd.add(PROD_GET_BYID); // cmd
                pData.setCmd(cmd);

                stat = this.updateSsnsProdiuctInventory(PIoper, banid, prodid, pData, dataObj, NAccObj);
                if (stat == true) {
                    boolean exist = false;
                    String key = NAccObj.getName()
                            + NAccObj.getCusid()
                            + NAccObj.getBanid()
                            + NAccObj.getTiid();
                    key = key.replaceAll(NAccObj.getUid(), "");
                    ArrayList<SsnsAcc> ssnsAccObjList = getSsnsDataImp().getSsnsAccObjList(NAccObj.getName(), NAccObj.getUid());
                    if (set.add(key)) {
                        if (ssnsAccObjList != null) {
                            if (ssnsAccObjList.size() != 0) {
                                SsnsAcc ssnsObj = ssnsAccObjList.get(0);
                                if (ssnsObj.getDown().equals("splunkflow")) {
                                    exist = true;
                                }
                            }
                        }
                    }
                    if (exist == false) {
                        ssnsAccObjList = getSsnsDataImp().getSsnsAccObjListByBan(NAccObj.getName(), NAccObj.getBanid());
                        if (ssnsAccObjList != null) {
                            if (ssnsAccObjList.size() > 3) {
                                exist = true;
                            }
                        }
                    }
                    if (exist == false) {
                        int ret = getSsnsDataImp().insertSsnsAccObject(NAccObj);
                    }
                }
            }
            return NAccObj.getName();
        } catch (Exception ex) {
            logger.info("> getFeatureSsnsProdiuctInventoryProcess Exception " + ex.getMessage());
        }
        return "";
    }

    public boolean updateSsnsProdiuctInventoryByProdId(String oper, String banid, String prodid, ProductData pData, SsnsData dataObj, SsnsAcc NAccObj) {
        try {
            String custId = dataObj.getCusid();
            String down = NAccObj.getDown();
            String feat = "";
            String outputSt = null;

            outputSt = SendSsnsProdiuctInventory(ServiceAFweb.URL_PRODUCT_PR, banid, prodid, oper, null);
            if (outputSt == null) {
                return false;
            }
            if (outputSt.indexOf("responseCode:400500") != -1) {
                return false;
            }

            if (oper.equals(APP_FEAT_TYPE_HSIC)) {
                feat = parseProductInternetFeature(outputSt, dataObj.getOper());

            } else if (oper.equals(APP_FEAT_TYPE_TTV)) {
                feat = parseProductTtvFeature(outputSt, dataObj.getOper());

            } else if (oper.equals(APP_FEATT_TYPE_SING)) {
                ArrayList returnParm = new ArrayList();
                feat = parseProductPhoneFeature(outputSt, dataObj.getOper(), returnParm);

                if (returnParm.size() > 0) {
                    custId = (String) returnParm.get(0);
                    //////////
//                    if (feat.indexOf("noCallControl") == -1) {
//                        if (feat.indexOf("fifa") != -1) {
//                            custId += ":FIFA";
//                            custId += ":VoiceMail";
//                            ArrayList cmd = new ArrayList();
//                            cmd = pData.getCmd();
//                            cmd.add("get Call control"); // description
//                            cmd.add(PROD_GET_CC); // cmd
//                            pData.setCmd(cmd);
//                        } else {
//                            custId += ":COMPASS";
//                            if (feat.indexOf("VoiceMail") != -1) {
//                                custId += ":VoiceMail";
//                            }
//                            ArrayList cmd = new ArrayList();
//                            cmd = pData.getCmd();
//                            cmd.add("get Call control"); // description
//                            cmd.add(PROD_GET_CC); // cmd
//                            pData.setCmd(cmd);
//                        }
//                        String CCparL[] = custId.split(":");
//
//                        String phone = CCparL[0];
//                        String sys = CCparL[1];
//                        String outputCCSt = SendSsnsCallControl(ServiceAFweb.URL_PRODUCT_PR, banid, phone, sys, null);
//                        String featCC = parseCallControlFeature(outputCCSt, dataObj.getOper());
//                        down = featCC;
//                    }
                    ///////////  
                }

            }

//            logger.info("> updateSsnsProdiuctInventory feat " + featTTV);
/////////////TTV   
            ArrayList<String> flow = new ArrayList();
            int faulure = getSsnsFlowTrace(dataObj, flow);
            if (flow == null) {
                logger.info("> updateSsnsProdiuctInventory skip no flow");
                return false;
            }

            pData.setFlow(flow);
            if (faulure == 1) {
                feat += ":failed";
            }
            logger.info("> updateSsnsProdiuctInventory feat " + feat);
            NAccObj.setName(feat);
            NAccObj.setCusid(custId);
            NAccObj.setBanid(banid);
            NAccObj.setUid(dataObj.getUid());
            NAccObj.setApp(dataObj.getApp());
            NAccObj.setTiid(dataObj.getTiid());
            NAccObj.setOper(oper);

            NAccObj.setDown(down);
            NAccObj.setRet(NAccObj.getRet());

            NAccObj.setExec(dataObj.getExec());

            String nameSt = new ObjectMapper().writeValueAsString(pData);
            NAccObj.setData(nameSt);

            NAccObj.setUpdatedatel(dataObj.getUpdatedatel());
            NAccObj.setUpdatedatedisplay(new java.sql.Date(dataObj.getUpdatedatel()));

            return true;
        } catch (Exception ex) {
            logger.info("> updateSsnsProdiuctInventory Exception " + ex.getMessage());
        }
        return false;
    }

    public boolean updateSsnsProdiuctInventory(String oper, String banid, String prodid, ProductData pData, SsnsData dataObj, SsnsAcc NAccObj) {
        try {
            String custId = dataObj.getCusid();
            String down = NAccObj.getDown();
            String outputSt = null;

            outputSt = SendSsnsProdiuctInventory(ServiceAFweb.URL_PRODUCT_PR, banid, prodid, oper, null);
            if (outputSt == null) {
                return false;
            }
            if (outputSt.indexOf("responseCode:400500") != -1) {
                return false;
            }
            String feat = "";
            if (oper.equals(SsnsService.APP_FEAT_TYPE_HSIC)) {
                feat = parseProductInternetFeature(outputSt, dataObj.getOper());

            } else if (oper.equals(SsnsService.APP_FEAT_TYPE_TTV)) {
                feat = parseProductTtvFeature(outputSt, dataObj.getOper());

            } else if (oper.equals(SsnsService.APP_FEATT_TYPE_SING)) {
                ArrayList returnParm = new ArrayList();
                feat = parseProductPhoneFeature(outputSt, dataObj.getOper(), returnParm);
                if (returnParm.size() > 0) {
                    custId = (String) returnParm.get(0);
                    //////////
//                    if (feat.indexOf("noCallControl") == -1) {
//                        if (feat.indexOf("fifa") != -1) {
//                            custId += ":FIFA";
//                            custId += ":VoiceMail";
//                            ArrayList cmd = new ArrayList();
//                            cmd = pData.getCmd();
//                            cmd.add("get Call control"); // description
//                            cmd.add(PROD_GET_CC); // cmd
//                            pData.setCmd(cmd);
//                        } else {
//                            custId += ":COMPASS";
//                            if (feat.indexOf("VoiceMail") != -1) {
//                                custId += ":VoiceMail";
//                            }
//                            ArrayList cmd = new ArrayList();
//                            cmd = pData.getCmd();
//                            cmd.add("get Call control"); // description
//                            cmd.add(PROD_GET_CC); // cmd
//                            pData.setCmd(cmd);
//                        }
//                        String CCparL[] = custId.split(":");
//
//                        String phone = CCparL[0];
//                        String sys = CCparL[1];
//                        String outputCCSt = SendSsnsCallControl(ServiceAFweb.URL_PRODUCT_PR, banid, phone, sys, null);
//                        String featCC = parseCallControlFeature(outputCCSt, dataObj.getOper());
//                        down = featCC;
//                    }
                    ///////////  
                }
            }

            if (feat == null) {
                return false;
            }

//            logger.info("> updateSsnsProdiuctInventory feat " + featTTV);
/////////////TTV  
            ArrayList<String> flow = new ArrayList();
            int faulure = getSsnsFlowTrace(dataObj, flow);
            if (flow == null) {
                logger.info("> updateSsnsProdiuctInventory skip no flow");
                return false;
            }

            pData.setFlow(flow);

            if (faulure == 1) {
                feat += ":failed";
            }
            logger.info("> updateSsnsProdiuctInventory feat " + feat);
            NAccObj.setName(feat);
            NAccObj.setCusid(custId);
            NAccObj.setBanid(banid);
            NAccObj.setUid(dataObj.getUid());
            NAccObj.setApp(dataObj.getApp());
            NAccObj.setTiid(dataObj.getTiid());
            NAccObj.setOper(oper);

            NAccObj.setDown(down);
            NAccObj.setRet(NAccObj.getRet());

            NAccObj.setExec(dataObj.getExec());

            String nameSt = new ObjectMapper().writeValueAsString(pData);
            NAccObj.setData(nameSt);

            NAccObj.setUpdatedatel(dataObj.getUpdatedatel());
            NAccObj.setUpdatedatedisplay(new java.sql.Date(dataObj.getUpdatedatel()));

            return true;
        } catch (Exception ex) {
            logger.info("> updateSsnsProdiuctInventory Exception " + ex.getMessage());
        }
        return false;
    }

    public static String parseProductPhoneFeature(String outputSt, String oper, ArrayList returnParm) {
        if (outputSt == null) {
            return "";
        }
        try {

            int callCInit = 0;
            int fifaInit = 0;
            int planInit = 0;
            int vmInit = 0;
            int LocalLine = 0;

            int isFIFA = 0;
            String PrimaryPricePlan = "";
            String CallControl = "";
            int voicemail = 0;
            String phoneN = "";
            ArrayList<String> outputList = ServiceAFweb.prettyPrintJSON(outputSt);

            for (int j = 0; j < outputList.size(); j++) {
                String inLine = outputList.get(outputList.size() - 1 - j);
//            logger.info("" + inLine);
                //"name":"isFIFA",
                if (inLine.indexOf("isFIFA") != -1) {
                    if (fifaInit == 1) {
                        continue;
                    }
                    fifaInit = 1;
                    String valueSt = outputList.get(outputList.size() - 1 - j + 1);
                    if (valueSt.indexOf("false") != -1) {
                        isFIFA = 0;
                    }
                    if (valueSt.indexOf("true") != -1) {
                        isFIFA = 1;
                    }
                    continue;
                }

                if (inLine.indexOf("CallControl") != -1) {

                    if (callCInit == 1) {
                        continue;
                    }
                    callCInit = 1;
                    String valueSt = checkProductNm(j, outputList);
                    if (valueSt.length() != 0) {
                        CallControl = valueSt;
                    }
                    continue;
                }
                if (inLine.indexOf("VoiceMail") != -1) {

                    if (vmInit == 1) {
                        continue;
                    }
                    vmInit = 1;
                    String valueSt = checkProductNm(j, outputList);
                    if (valueSt.length() != 0) {
                        voicemail = 1;
                    }
                    continue;
                }

                if (inLine.indexOf("primaryServiceResourceValue") != -1) {
                    String valueSt = outputList.get(outputList.size() - 1 - j + 1);
                    valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                    valueSt = ServiceAFweb.replaceAll("value:", "", valueSt);
                    phoneN = valueSt;
                    if (returnParm != null) {
                        returnParm.add(phoneN);
                    }
                    continue;
                }
                if (inLine.indexOf("LocalLine") != -1) {
                    if (planInit == 1) {
                        continue;
                    }
                    LocalLine = 1;
                    continue;
                }

                if (inLine.indexOf("HomePhoneBundle") != -1) {
                    if (planInit == 1) {
                        continue;
                    }
                    planInit = 1;
                    String valueSt = checkProductOfferingProductNm(outputList.size() - 1 - j + 1, outputList);
                    if (valueSt.length() != 0) {
                        PrimaryPricePlan = valueSt;
                    } else {
                        valueSt = checkProductOfferingNextProductNmForSING(outputList.size() - 1 - j + 1, outputList);
//                        valueSt = checkProductNm(outputList.size() - 1 - j + 1, outputList);
                        if (valueSt.length() != 0) {
                            PrimaryPricePlan = valueSt;
                        }
                    }
                    continue;
                }
            }
            String featTTV = APP_FEATT_TYPE_SING;
            featTTV += ":" + oper;
            String fifa = "fifa";
            if (isFIFA == 0) {
                fifa = "comp";
            }
            featTTV += ":" + fifa;
            String vm = "VoiceMail";
            if (voicemail == 0) {
                vm = "noVoliceM";
            }
            featTTV += ":" + vm;
            String plan = PrimaryPricePlan;
            if (plan.length() == 0) {
                plan = "noPlan";
                if (LocalLine == 1) {
                    plan = "LocalLine";
                }
            }
            featTTV += ":" + plan;
            String callC = CallControl;
            if (callC.length() == 0) {
                callC = "noCallControl";
            }
            featTTV += ":" + callC;

            return featTTV;
        } catch (Exception ex) {

        }
        return "";
    }

    public static String checkProductRelationshipProductNm(int j, ArrayList<String> outputList) {
        for (int k = j; k < outputList.size(); k++) {
            String inL = outputList.get(outputList.size() - 1 - k);
            if (inL.indexOf("productRelationship") != -1) {
                for (int m = k; m < outputList.size(); m++) {
                    String inLL = outputList.get(outputList.size() - 1 - m);
                    if (inLL.indexOf("productNm") != -1) {
                        String valueSt = outputList.get(outputList.size() - 1 - m + 1);
                        valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                        valueSt = ServiceAFweb.replaceAll("value:", "", valueSt);
                        valueSt = ServiceAFweb.replaceAll(" ", "_", valueSt);
                        return valueSt;
                    }
                }
            }
        }
        return "";
    }

    public static String checkProductNm(int j, ArrayList<String> outputList) {
        for (int k = j; k < outputList.size(); k++) {
            String inL = outputList.get(outputList.size() - 1 - k);
            if (inL.indexOf("productNm") != -1) {
                String valueSt = outputList.get(outputList.size() - 1 - k + 1);
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("value:", "", valueSt);
                valueSt = ServiceAFweb.replaceAll(" ", "_", valueSt);
                return valueSt;
            }
        }
        return "";
    }

    public static String checkProductOfferingProductNm(int j, ArrayList<String> outputList) {
        try {
            for (int k = j; k < outputList.size(); k++) {
                String inL = outputList.get(outputList.size() - 1 - k);
                if (inL.indexOf("productOffering") != -1) {
                    for (int m = k; m < outputList.size(); m++) {
                        String inLL = outputList.get(outputList.size() - 1 - m);
                        if (inLL.indexOf("productNm") != -1) {
                            String valueSt = outputList.get(outputList.size() - 1 - m + 1);
                            valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                            valueSt = ServiceAFweb.replaceAll("value:", "", valueSt);
                            valueSt = ServiceAFweb.replaceAll(" ", "_", valueSt);
                            return valueSt;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.info("> checkProductOfferingProductNm " + ex.getMessage());
        }
        return "";
    }

    public static String checkProductOfferingNextProductNmForSING(int j, ArrayList<String> outputList) {
        try {
            for (int k = j; k < outputList.size(); k++) {
                String inL = outputList.get(outputList.size() - 1 - k);
                if (inL.indexOf("productOffering") != -1) {
                    int tmp = outputList.size() - 1 - k;
                    for (int m = tmp; m < outputList.size(); m++) {
                        String inLL = outputList.get(m);// (outputList.size() - 1 - m);
                        if (inLL.indexOf("productNm") != -1) {
                            String valueSt = outputList.get(m + 1);
                            valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                            valueSt = ServiceAFweb.replaceAll("value:", "", valueSt);
                            valueSt = ServiceAFweb.replaceAll(" ", "_", valueSt);
                            return valueSt;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.info("> checkProductOfferingProductNm " + ex.getMessage());
        }
        return "";
    }

    public static String parseCallControlFeature(String outputSt, String oper, String host) {

        if (outputSt == null) {
            return "";
        }
        try {

            String whiteList = "noWhiteL";
            String blackList = "noBackL";
            String spamOn = "spam_";
            String premiumService = "premiumS_";
            String vmWithTelus = "vm_";
            ArrayList<String> outputList = ServiceAFweb.prettyPrintJSON(outputSt);

            for (int j = 0; j < outputList.size(); j++) {
                String inLine = outputList.get(j);
//                        logger.info("" + inLine);

                if (inLine.indexOf("whiteList") != -1) {
                    whiteList = "whiteList";
                    continue;
                }
                if (inLine.indexOf("blackList") != -1) {
                    blackList = "blackList";
                    continue;
                }
                if (inLine.indexOf("spamOn") != -1) {
                    String valueSt = outputList.get(j + 1);
                    valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                    valueSt = ServiceAFweb.replaceAll("value:", "", valueSt);
                    spamOn += valueSt;
                    continue;
                }
                if (inLine.indexOf("premiumService") != -1) {
                    String valueSt = outputList.get(j + 1);
                    valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                    valueSt = ServiceAFweb.replaceAll("value:", "", valueSt);
                    premiumService += valueSt;
                    continue;
                }
                if (inLine.indexOf("vmWithTelus") != -1) {
                    String valueSt = outputList.get(j + 1);
                    valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                    valueSt = ServiceAFweb.replaceAll("value:", "", valueSt);
                    vmWithTelus += valueSt;
                    continue;
                }
            }

            String featTTV = APP_FEATT_TYPE_CC;
            featTTV += ":" + oper + ":" + host;
            featTTV += ":" + whiteList;
            featTTV += ":" + blackList;
            featTTV += ":" + spamOn;
            featTTV += ":" + premiumService;
            featTTV += ":" + vmWithTelus;

            return featTTV;
        } catch (Exception ex) {

        }
        return "";
    }

    public static String parseProductInternetFeature(String outputSt, String oper) {

        if (outputSt == null) {
            return "";
        }
        try {

            int quotaAmtInit = 0;
            int fifaInit = 0;
            int planInit = 0;
            int fifaFlag = 0;
            int isFIFA = 0;

            String SecurityBundle = "";
            String EmailFeatures = "";
            String UnlimitedUsage = "";
            String PrimaryPricePlan = "";

            ArrayList<String> outputList = ServiceAFweb.prettyPrintJSON(outputSt);

            for (int j = 0; j < outputList.size(); j++) {
                String inLine = outputList.get(j);
//                        logger.info("" + inLine);
                //"name":"isFIFA",
                if (inLine.indexOf("isFIFA") != -1) {
                    if (fifaInit == 1) {
                        continue;
                    }
                    fifaInit = 1;
                    String valueSt = outputList.get(j + 1);
                    if (valueSt.indexOf("false") != -1) {
                        isFIFA = 0;
                        fifaFlag = 0;
                    }
                    if (valueSt.indexOf("true") != -1) {
                        isFIFA = 1;
                        fifaFlag = 1;
                    }
                    break;
                }
            }

            for (int j = 0; j < outputList.size(); j++) {
                String inLine = outputList.get(outputList.size() - 1 - j);
//            logger.info("" + inLine);

                if (inLine.indexOf("isFIFA") != -1) {
                    if (fifaInit == 1) {
                        continue;
                    }
                    fifaInit = 1;
                    String valueSt = outputList.get(j - 1);
                    if (valueSt.indexOf("false") != -1) {
                        isFIFA = 0;
                    }
                    if (valueSt.indexOf("true") != -1) {
                        isFIFA = 1;
                    }
                    continue;
                }
                if ((inLine.indexOf("SecurityBundle") != -1)
                        || (inLine.indexOf("TELUSOnlineSec") != -1)) {

                    if (quotaAmtInit == 1) {
                        continue;
                    }
                    quotaAmtInit = 1;
                    boolean exit = false;

                    String valueSt = checkProductNm(j, outputList);
                    valueSt = valueSt.replaceAll("_", "");
                    SecurityBundle = valueSt;

                    continue;
                }

                if (inLine.indexOf("PrimaryPricePlan") != -1) {
                    if (planInit == 1) {
                        continue;
                    }

                    planInit = 1;
                    if (fifaFlag == 0) {
                        String valueSt = checkProductRelationshipProductNm(j, outputList);
                        PrimaryPricePlan = valueSt;

                    } else if (fifaFlag == 1) {
                        String valueSt = checkProductNm(j, outputList);
                        if (valueSt.length() != 0) {
                            PrimaryPricePlan = valueSt;
                        }
                    }
                    continue;
                }
                if (inLine.indexOf("EmailFeatures") != -1) {
                    EmailFeatures = "EmailFeatures";
                    continue;
                }
                if (inLine.indexOf("UnlimitedUsage") != -1) {
                    UnlimitedUsage = "UnlimitedUsage";
                    continue;
                }
            }
            String featTTV = APP_FEAT_TYPE_HSIC;
            featTTV += ":" + oper;
            String fifa = "fifa";
            if (isFIFA == 0) {
                fifa = "comp";
            }
            featTTV += ":" + fifa;
            featTTV += ":" + PrimaryPricePlan;
            String security = SecurityBundle;
            if (security.length() == 0) {
                security = "noSecurity";
            }
            featTTV += ":" + security;

            String mail = EmailFeatures;
            if (mail.length() == 0) {
                mail = "noEmail";
            } else {
                mail = "Email";
            }
            featTTV += ":" + mail;

            String unlimit = UnlimitedUsage;
            if (unlimit.length() == 0) {
                unlimit = "noUnlimitedU";
            } else {
                unlimit = "UnlimitedU";
            }
            featTTV += ":" + unlimit;

            return featTTV;
        } catch (Exception ex) {

        }
        return "";
    }

    public static String parseProductTtvFeature(String outputSt, String oper) {

        if (outputSt == null) {
            return "";
        }

        int productCdInit = 0;
        int ChannelListInit = 0;
        int offerInit = 0;
        int fifaInit = 0;
        int regionInit = 0;

        int isFIFA = 0;
        String offer = "noOfferCd";
        String productCd = "Essentials";
        int ChannelList = 0;
        String region = "";

        ArrayList<String> outputList = ServiceAFweb.prettyPrintJSON(outputSt);
        for (int j = 0; j < outputList.size(); j++) {
            String inLine = outputList.get(j);
//                        logger.info("" + inLine);
            //"name":"isFIFA",
            if (inLine.indexOf("isFIFA") != -1) {
                if (fifaInit == 1) {
                    continue;
                }
                fifaInit = 1;
                String valueSt = outputList.get(j + 1);
                if (valueSt.indexOf("false") != -1) {
                    isFIFA = 0;
                }
                if (valueSt.indexOf("true") != -1) {
                    isFIFA = 1;
                }
                continue;
            }
            if (inLine.indexOf("offer") != -1) {
                if (offerInit == 1) {
                    continue;
                }
                offerInit = 1;
                String valueSt = outputList.get(j + 1);
                if (valueSt.indexOf("MediaroomTV-HS2.0") != -1) {
                    offer = "Mediaroom20";
                    continue;
                }
                if (valueSt.indexOf("MediaroomTV-HS") != -1) {
                    offer = "Mediaroom";
                    continue;
                }
                if (valueSt.indexOf("TVX") != -1) {
                    offer = "TVX";
                }
                continue;
            }

            if (inLine.indexOf("region") != -1) {
                if (regionInit == 1) {
                    continue;
                }
                regionInit = 1;
                String valueSt = outputList.get(j + 1);
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("value:", "", valueSt);
                region = valueSt;
                continue;
            }
            if (inLine.indexOf("productCd") != -1) {
                if (productCdInit == 1) {
                    continue;
                }
                productCdInit = 1;
                String valueSt = outputList.get(j + 1);
                valueSt = ServiceAFweb.replaceAll("\"", "", valueSt);
                valueSt = ServiceAFweb.replaceAll("value:", "", valueSt);
                productCd = valueSt;
                continue;
            }
            if (inLine.indexOf("ChannelList") != -1) {
                if (ChannelListInit == 1) {
                    continue;
                }
                ChannelListInit = 1;
                String valueSt = outputList.get(j + 3);
                if (valueSt.indexOf("channelId") != -1) {
                    ChannelList = (1);
                } else {
                    ChannelList = (0);
                }
                continue;
            }
        }

        String featTTV = APP_FEAT_TYPE_TTV;
        featTTV += ":" + oper;
        String fifa = "fifa";
        if (isFIFA == 0) {
            fifa = "comp";
        }
        featTTV += ":" + fifa;
//        featTTV += ":" + region;
        featTTV += ":" + offer;
        featTTV += ":" + productCd;

        String chann = "ChListfailed";
        if (ChannelList == 1) {
            chann = "ChannelList";
        }
        featTTV += ":" + chann;

        if (region.length() == 0) {
            featTTV += ":noRegion";
        }
        return featTTV;
    }

    public String SendSsnsProdiuctInventoryByProdId(String ProductURL, String ban, String prodid, ArrayList<String> inList) {
        logger.info("> SendSsnsProdiuctInventory " + ban + " " + prodid);

        String url = ProductURL + "/v1/cmo/selfmgmt/productinventory/product/" + prodid + "?billingAccount.id=" + ban;
        try {
            if (inList != null) {
                inList.add(url);
            }
            // calculate elapsed time in milli seconds
            long startTime = TimeConvertion.currentTimeMillis();

            String output = this.sendRequest_Ssns(METHOD_GET, url, null, null, null);

            long endTime = TimeConvertion.currentTimeMillis();
            long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
            if (inList != null) {
                String tzid = "America/New_York"; //EDT
                TimeZone tz = TimeZone.getTimeZone(tzid);
                Date d = new Date(startTime);
                // timezone symbol (z) included in the format pattern 
                DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                // format date in target timezone
                format.setTimeZone(tz);
                String ESTdate = format.format(d);

                inList.add(ESTdate + " elapsedTime:" + elapsedTime);
                inList.add("output:");
            }
            return output;
        } catch (Exception ex) {
            logger.info("> SsnsProdiuctInventory exception " + ex.getMessage());
        }
        return null;
    }

    public String SendSsnsProdiuctInventory(String ProductURL, String ban, String prodid, String productType, ArrayList<String> inList) {
        logger.info("> SendSsnsProdiuctInventory " + ban + " " + prodid + " " + productType);

        String url = "";
        if (prodid.length() == 0) {
            url = ProductURL + "/v1/cmo/selfmgmt/productinventory/product?billingAccount.id=" + ban
                    + "&productType=" + productType;
            if (productType.equals(APP_FEAT_TYPE_TTV)) {
                url += "&fields=product.characteristic.channelInfoList";
//            } else if (productType.equals(APP_FEATT_TYPE_SING)) {
//                url += "&fields=product.characteristic.voicemail";
            }
        } else {
            url = ProductURL + "/v1/cmo/selfmgmt/productinventory/product/" + prodid + "?billingAccount.id=" + ban;
            if (productType.equals(APP_FEAT_TYPE_TTV)) {
                url += "&fields=product.characteristic.channelInfoList";
            } else if (productType.equals(APP_FEATT_TYPE_SING)) {
                url += "&fields=product.characteristic.voicemail";
            }
        }

        try {
            if (inList != null) {
                inList.add(url);
            }
            // calculate elapsed time in milli seconds
            long startTime = TimeConvertion.currentTimeMillis();

            String output = this.sendRequest_Ssns(METHOD_GET, url, null, null, null);

            long endTime = TimeConvertion.currentTimeMillis();
            long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
            if (inList != null) {
                String tzid = "America/New_York"; //EDT
                TimeZone tz = TimeZone.getTimeZone(tzid);
                Date d = new Date(startTime);
                // timezone symbol (z) included in the format pattern 
                DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                // format date in target timezone
                format.setTimeZone(tz);
                String ESTdate = format.format(d);

                inList.add(ESTdate + " elapsedTime:" + elapsedTime);
                inList.add("output:");
            }

            return output;
        } catch (Exception ex) {
            logger.info("> SsnsProdiuctInventory exception " + ex.getMessage());
        }
        return null;
    }

    public String SendSsnsCallControl(String ProductURL, String ban, String phoneNum, String sys, ArrayList<String> inList) {
        logger.info("> SendSsnsCallControl " + ban + " " + phoneNum + " " + sys);
        String url = "";
        if ((ban.length() == 0) || (phoneNum.length() == 0) || (sys.length() == 0)) {
            return null;
        }
        url = ProductURL + "/v1/cmo/selfmgmt/callcontrolmanagement/callcontrol/" + phoneNum
                + "?relatedpartylist.id=" + ban
                + "&characteristiclist.system=" + sys;

        try {
            if (inList != null) {
                inList.add(url);
            }
            // calculate elapsed time in milli seconds
            long startTime = TimeConvertion.currentTimeMillis();

            String output = this.sendRequest_Ssns(METHOD_GET, url, null, null, null);

            long endTime = TimeConvertion.currentTimeMillis();
            long elapsedTime = endTime - startTime;
//            System.out.println("Elapsed time in milli seconds: " + elapsedTime);
            if (inList != null) {
                String tzid = "America/New_York"; //EDT
                TimeZone tz = TimeZone.getTimeZone(tzid);
                Date d = new Date(startTime);
                // timezone symbol (z) included in the format pattern 
                DateFormat format = new SimpleDateFormat("M/dd/yyyy hh:mm a z");
                // format date in target timezone
                format.setTimeZone(tz);
                String ESTdate = format.format(d);

                inList.add(ESTdate + " elapsedTime:" + elapsedTime);
                inList.add("output:");
            }

            return output;
        } catch (Exception ex) {
            logger.info("> SsnsProdiuctInventory exception " + ex.getMessage());
        }
        return null;
    }

    // 1 faulure, 0 = success
    public int getSsnsFlowTrace(SsnsData dataObj, ArrayList<String> flow) {

        String uid = dataObj.getUid();
        int failure = 0;

        ArrayList<SsnsData> ssnsList = getSsnsDataImp().getSsnsDataObjListByUid(dataObj.getApp(), uid);
        if (ssnsList != null) {
//            logger.info("> ssnsList " + ssnsList.size());
            for (int i = 0; i < ssnsList.size(); i++) {
                SsnsData data = ssnsList.get(i);
                String down = data.getDown();
                if (down.length() == 0) {
                    continue;
                }
                String flowSt = data.getDown();
                if (flowSt.length() == 0) {
                    flowSt = data.getOper();
                }
                flowSt += ":" + data.getExec();
                String dataTxt = data.getData();
                if (dataTxt.indexOf("stacktrace") != -1) {
                    failure = 1;
                } else {
                    dataTxt = data.getRet();
                    if (dataTxt.indexOf("httpCd=500") != -1) {
                        failure = 1;
                    }
                }
//                logger.info("> flow " + flowSt);
                if (failure == 1) {
                    flowSt += ":failed:" + data.getData();
                }
                flow.add(flowSt);
            }
        }
        return failure;
    }

    /////////////////////////////////////////////////////////////
    // operations names constants
    private static final String METHOD_POST = "post";
    private static final String METHOD_GET = "get";

    public String sendRequest_Ssns(String method, String subResourcePath, Map<String, String> queryParams,
            Map<String, String> bodyParams, String postParmSt) throws Exception {
        String response = null;
        for (int i = 0; i < 4; i++) {

            try {

                response = sendRequest_Process_Ssns(method, subResourcePath, queryParams, bodyParams, postParmSt);
                if (response != null) {
                    return response;
                }
                ServiceAFweb.AFSleep1Sec(i);
            } catch (Exception ex) {
                logger.info("sendRequest " + method + " Rety " + (i + 1));
            }
        }

        response = sendRequest_Process_Ssns(method, subResourcePath, queryParams, bodyParams, postParmSt);
        return response;
    }

    private String sendRequest_Process_Ssns(String method, String subResourcePath, Map<String, String> queryParams, Map<String, String> bodyParams, String postParmSt) {

        try {
//            if (CKey.SQL_Devop == true) {
//                if (subResourcePath.indexOf("DEVOP") != -1) {
//                    // send to devop client
//
//                    return sendRequest_Process_Devop(method, subResourcePath, queryParams, bodyParams);
//                }
//            }
            if (subResourcePath.indexOf("https") != -1) {
                return this.https_sendRequest_Process_Ssns(method, subResourcePath, queryParams, bodyParams, postParmSt);
            }
            return this.http_sendRequest_Process_Ssns(method, subResourcePath, queryParams, bodyParams, postParmSt);
        } catch (Exception ex) {
//            Logger.getLogger(SsnsService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

//    private String sendRequest_Process_Devop(String method, String subResourcePath, Map<String, String> queryParams, Map<String, String> bodyParams)
//            throws Exception {
//        try {
//
//            String URLPath = subResourcePath;
//
//            String webResourceString = "";
//            // assume only one param
//            if (queryParams != null && !queryParams.isEmpty()) {
//                for (String key : queryParams.keySet()) {
//                    webResourceString = "?" + key + "=" + queryParams.get(key);
//                }
//            }
//
//            String bodyElement = "";
//            if (bodyParams != null) {
//                bodyElement = new ObjectMapper().writeValueAsString(bodyParams);
//            }
//            URLPath += webResourceString;
//
//            ServiceAFwebREST remoteREST = new ServiceAFwebREST();
//            RequestObj sqlObj = new RequestObj();
//            String cmd = "99";
//            sqlObj.setCmd(cmd);
//            sqlObj.setReq(method);
//            sqlObj.setReq1(URLPath);
//            sqlObj.setReq2(bodyElement);
//            String resp = remoteREST.getSQLRequestRemote(sqlObj);
//            return resp;
//
//        } catch (Exception e) {
////            logger.info("Error sending REST request:" + e);
//            throw e;
//        }
//    }
    private String https_sendRequest_Process_Ssns(String method, String subResourcePath, Map<String, String> queryParams,
            Map<String, String> bodyParams, String postParmSt)
            throws Exception {
        try {

            String URLPath = subResourcePath;

            String webResourceString = "";
            // assume only one param
            if (queryParams != null && !queryParams.isEmpty()) {
                for (String key : queryParams.keySet()) {
                    webResourceString = "?" + key + "=" + queryParams.get(key);
                }
            }

            String bodyElement = "";
            if (bodyParams != null) {
                bodyElement = new ObjectMapper().writeValueAsString(bodyParams);
            }
            if (postParmSt != null) {
                bodyElement = postParmSt;
            }
            URLPath += webResourceString;
            URL request = new URL(URLPath);

            HttpsURLConnection con = null; //(HttpURLConnection) request.openConnection();

//            if (CKey.PROXY == true) {
//                //////Add Proxy 
//                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ServiceAFweb.PROXYURL, 8080));
//                con = (HttpsURLConnection) request.openConnection(proxy);
//                //////Add Proxy 
//            } else {
            con = (HttpsURLConnection) request.openConnection();
//            }

//            if (URLPath.indexOf(":8080") == -1) {
            String authStr = "APP_SELFSERVEUSGBIZSVC" + ":" + "soaorgid";
            // encode data on your side using BASE64
            byte[] bytesEncoded = Base64.encodeBase64(authStr.getBytes());
            String authEncoded = new String(bytesEncoded);
            con.setRequestProperty("Authorization", "Basic " + authEncoded);
//            }

            if (method.equals(METHOD_POST)) {
                con.setRequestMethod("POST");
            } else if (method.equals(METHOD_GET)) {
                con.setRequestMethod("GET");
            }
            ///////////
//            logger.info("POST request method:" + method + " host: " + request.getHost() + " url: " + request.getPath());
            ///////            
            con.setRequestProperty("User-Agent", USER_AGENT);
//            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");

            if (method.equals(METHOD_POST)) {

//                con.setRequestMethod("POST");
//                con.addRequestProperty("Accept", "application/json");
//                con.addRequestProperty("Connection", "close");
//                con.addRequestProperty("Content-Encoding", "gzip"); // We gzip our request
//                con.addRequestProperty("Content-Length", String.valueOf(bodyElement.length()));
//                con.setRequestProperty("Content-Type", "application/json"); // We send our data in JSON format
                con.setDoInput(true);
                // For POST only - START                
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                byte[] input = bodyElement.getBytes("utf-8");
                os.write(input, 0, input.length);
                os.flush();
                os.close();
                // For POST only - END
            }

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Response Code:: " + responseCode);

//                if ((responseCode == 400) || (responseCode == 500)) {
                InputStream inputstream = null;
                inputstream = con.getErrorStream();

                StringBuffer response = new StringBuffer();
                response.append(URLPath);
                BufferedReader in = new BufferedReader(new InputStreamReader(inputstream));
                String line;
                response.append("responseCode:400500");
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                System.out.println(response.toString());
                return response.toString();
//                }
            }
            if (responseCode >= 200 && responseCode < 300) {
                ;
            } else {
                System.out.println("Response Code:: " + responseCode);
//                System.out.println("bodyElement :: " + bodyElement);
                return null;
            }

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;

                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // print result
                return response.toString();
            } else {
                logger.info("POST request not worked");
            }

        } catch (Exception e) {
            logger.info("Error sending REST request:" + e);
            throw e;
        }
        return null;
    }

    private String http_sendRequest_Process_Ssns(String method, String subResourcePath, Map<String, String> queryParams,
            Map<String, String> bodyParams, String postParmSt)
            throws Exception {
        try {

            String URLPath = subResourcePath;

            String webResourceString = "";
            // assume only one param
            if (queryParams != null && !queryParams.isEmpty()) {
                for (String key : queryParams.keySet()) {
                    webResourceString = "?" + key + "=" + queryParams.get(key);
                }
            }

            String bodyElement = "";
            if (bodyParams != null) {
                bodyElement = new ObjectMapper().writeValueAsString(bodyParams);
            }
            if (postParmSt != null) {
                bodyElement = postParmSt;
            }
            URLPath += webResourceString;
            URL request = new URL(URLPath);

            HttpURLConnection con = null; //(HttpURLConnection) request.openConnection();

//            if (CKey.PROXY == true) {
//                //////Add Proxy 
//                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ServiceAFweb.PROXYURL, 8080));
//                con = (HttpURLConnection) request.openConnection(proxy);
//                //////Add Proxy 
//            } else {
            con = (HttpURLConnection) request.openConnection(Proxy.NO_PROXY);
//            }

//            if (URLPath.indexOf(":8080") == -1) {
            String authStr = "APP_SELFSERVEUSGBIZSVC" + ":" + "soaorgid";
            // encode data on your side using BASE64
            byte[] bytesEncoded = Base64.encodeBase64(authStr.getBytes());
            String authEncoded = new String(bytesEncoded);
            con.setRequestProperty("Authorization", "Basic " + authEncoded);
//            }

            if (method.equals(METHOD_POST)) {
                con.setRequestMethod("POST");
            } else if (method.equals(METHOD_GET)) {
                con.setRequestMethod("GET");
            }
            ///////////
//            logger.info("POST request method:" + method + " host: " + request.getHost() + " url: " + request.getPath());
            ///////            

            con.setRequestProperty("User-Agent", USER_AGENT);
//            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
//            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");

            if (method.equals(METHOD_POST)) {
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = bodyElement.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                    os.close();
                }

            }

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
//                System.out.println("Response Code:: " + responseCode);
//                if ((responseCode == 400) || (responseCode == 500)) {
                InputStream inputstream = null;
                inputstream = con.getErrorStream();

                StringBuffer response = new StringBuffer();
                response.append(URLPath);
                BufferedReader in = new BufferedReader(new InputStreamReader(inputstream));
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                System.out.println(response.toString());
                return response.toString();
//                }
            }
            if (responseCode >= 200 && responseCode < 300) {
                ;
            } else {
//                System.out.println("Response Code:: " + responseCode);
//                System.out.println("bodyElement :: " + bodyElement);
                return null;
            }
            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;

                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {

                    response.append(inputLine);
                }
                in.close();
                // print result
                return response.toString();
            } else {
                logger.info("POST request not worked");
            }

        } catch (Exception e) {
//            logger.info("Error sending REST request:" + e);
            throw e;
        }
        return null;
    }

    ////////
    public static String[] splitIncludeEmpty(String inputStr, char delimiter) {
        if (inputStr == null) {
            return null;
        }
        if (inputStr.charAt(inputStr.length() - 1) == delimiter) {

            inputStr += "End";
            String[] tempString = inputStr.split("" + delimiter);
            int size = tempString.length - 1;
            String[] outString = new String[size];
            for (int i = 0; i < size; i++) {
                outString[i] = tempString[i];
            }
            return outString;
        }
        return inputStr.split("" + delimiter);
    }

//https://self-learning-java-tutorial.blogspot.com/2018/03/pretty-print-xml-string-in-java.html
//    public static String getPrettyXMLString(String xmlData, int indent) throws Exception {
//        TransformerFactory transformerFactory = TransformerFactory.newInstance();
//        transformerFactory.setAttribute("indent-number", indent);
//
//        Transformer transformer = transformerFactory.newTransformer();
//        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//
//        StringWriter stringWriter = new StringWriter();
//        StreamResult xmlOutput = new StreamResult(stringWriter);
//
//        Source xmlInput = new StreamSource(new StringReader(xmlData));
//        transformer.transform(xmlInput, xmlOutput);
//
//        return xmlOutput.getWriter().toString();
//    }
    /**
     * @return the ssnsDataImp
     */
    public SsnsDataImp getSsnsDataImp() {
        return ssnsDataImp;
    }

    /**
     * @param ssnsDataImp the ssnsDataImp to set
     */
    public void setSsnsDataImp(SsnsDataImp ssnsDataImp) {
        this.ssnsDataImp = ssnsDataImp;
    }

}
