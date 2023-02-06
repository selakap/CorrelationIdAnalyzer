package custom.correlationAnalyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class analyzer implements Runnable {
    public static List<Object> nonmatchedcorrelationIdList = new ArrayList();;
    private Object correlationIdentifier;
    public static List correlationIdList = new ArrayList();//To store the correlation ids in correlation.log file

    public analyzer(Object Id) {
        this.correlationIdentifier = Id;
        // store parameter for later user
    }

    public static void main(String[] args) {

        try{
            //Creating correlation id txt file======================================================================
            try {
                File myObj = new File("/Users/selakapiumal/Desktop/correlatioAnalyzer/correlationIds.txt");
                File myObj1 = new File("/Users/selakapiumal/Desktop/correlatioAnalyzer/nonmatchedcorrelationIds.txt");

                if (myObj.createNewFile()&&myObj1.createNewFile()) {
                    System.out.println("File created: " + myObj.getName());
                } else {
                    System.out.println("File already exists.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }

            //===================================================================================================

            //Read the correlation.log file ====================================================================
            FileInputStream correlationLog = new FileInputStream("/Users/selakapiumal/Desktop/correlatioAnalyzer/correlation.log");
            BufferedReader br = new BufferedReader(new InputStreamReader(correlationLog));
            String strLine;


            /* read log line by line */
            while ((strLine = br.readLine()) != null)   {
                if (strLine.split("\\|").length > 5 ){

                    String correlationId = (strLine.split("\\|"))[1];
                    System.out.println(correlationId);
                    if (!correlationIdList.contains(correlationId)){
                        correlationIdList.add(correlationId);
                    }
                }
            }

            correlationLog.close();
            //===================================================================================================

            //Save all the correlation ids (optional - this is to track the IDs if needed) =========================
            FileWriter myWriter = new FileWriter("/Users/selakapiumal/Desktop/correlatioAnalyzer/correlationIds.txt");
            System.out.println("id count: "+correlationIdList.size()+" List: "+correlationIdList);
            myWriter.write(correlationIdList.toString());
            myWriter.close();

            //======================================================================================================

            //check the non matching ids in carbon log and save it in nonmatchedcorrelationIds.txt==============================================================


            ExecutorService executor = Executors.newFixedThreadPool(100);
            for (Object Id : correlationIdList){

                Runnable worker = new analyzer(Id);
                executor.execute(worker);

            }


            //====================================================================================================


        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        //System.out.println("inside run" + correlationIdentifier.toString());
        String grepCommand = "grep " + correlationIdentifier.toString() +" /Users/selakapiumal/Desktop/correlatioAnalyzer/wso2carbon.log";
        Process proc = null;


        try {
            proc = Runtime.getRuntime().exec(grepCommand);
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            if ((line = reader.readLine()) != null){
                System.out.print("existing id" + "\n");

            }else {
                nonmatchedcorrelationIdList.add(correlationIdentifier.toString());
                System.out.print("missing id" + correlationIdentifier.toString()+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (correlationIdentifier == correlationIdList.get(correlationIdList.size() - 1)){
            System.out.println("Non matching ids:  " +nonmatchedcorrelationIdList);
            //Save non matching ids ===============================================================================

            try {
                FileWriter myWriter1 = new FileWriter("/Users/selakapiumal/Desktop/correlatioAnalyzer/nonmatchedcorrelationIds.txt");
                myWriter1.write(nonmatchedcorrelationIdList.toString());
                myWriter1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //====================================================================================================

        }
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
