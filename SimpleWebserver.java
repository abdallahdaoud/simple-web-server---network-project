
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class SimpleWebserver {
    public static void main(String[] args) throws IOException {        
        ServerSocket serverSocket = new ServerSocket(6500);
        System.out.println("Server is listening on port: " + 6500);
        processingClientRequest(serverSocket);
    }
    private static void processingClientRequest(ServerSocket serverSocket) throws IOException {
        int i=0;
        while(true) {
            Socket clientSocket = serverSocket.accept(); //whating for client request.
            System.out.println("Client connected");
            
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())); 
                //to get and deal with request.
            String clientRequest = in.readLine(); 
            //first line of request has all things we need to know about the request.
            System.out.println("------------------Start Request---------------");
            System.out.println(clientRequest); 
            
            String cr;
            while((cr = in.readLine())!=null){
                System.out.println(cr);
                if(cr.isEmpty()){
                    break;
                }
            } //print the request.

            if(clientRequest==null) continue; //do nothing when received null request.

            clientRequest = clientRequest.toLowerCase();
            //all files have to be in lower case to access to avoid problems when 
            //access some files. for example: /A.txt request will access the file /a.txt
            //some time server was litter sensitive.

            OutputStream clientOutput = clientSocket.getOutputStream();
            //using to write response.

            int g = clientRequest.indexOf("get");
            int h = clientRequest.indexOf("http/1.1");
            boolean errorFlag = true;
            if((g>-1 && g<2) && (h>-1 && h>clientRequest.length()-10)) {
                //if 'get' in first of the request line and 'http/1.1' in last 
                //then check if the required file is valid.

                String filePath = "all" + clientRequest.substring(g+4, h-1);
                //get "/filePath.type" then abend with all like that "all/filePath.type".

                //set the correct directory befor send the response 
                if(filePath.equals("all/index.html") || filePath.equals("all/")) 
                    filePath = "all/main.html"; 

                //sort and set the correct directory befor send the response 
                if(filePath.equals("all/sortbyprice") || filePath.equals("all/")) {
                    sortBy(true);
                    filePath = "all/sortByPrice.html";
                }
                if(filePath.equals("all/sortbyname") || filePath.equals("all/")) {
                    sortBy(false);
                    filePath = "all/sortByName.html";
                }
                if(Files.exists(Paths.get(filePath)) && 
                 !Files.isDirectory(Paths.get(filePath))) {
                    String type = filePath.substring(filePath.indexOf('.')+1);
                    type = (type.equals("jpg") || type.equals("png") 
                            || type.equals("ico")) ? "imag/"+type : "text/"+type; 
                    //to indicate the 'Content-type' in the response.

                    sendRespons(clientOutput, "200 OK", type, filePath);
                    errorFlag = false; //when the request is 'OK'.
                }
            }  
            if(errorFlag) { //when the request is 'bad'.

                FileWriter ipFileWriter = new FileWriter("all/ip.html");
                ipFileWriter.write("<p style=\"text-align: center;\">"
                 + clientSocket.toString()+"</p>");
                //writ IP and port number of the client to 'ip.html' to be used by 
                //'error.html'.
                ipFileWriter.close();

                sendRespons(clientOutput, "404 Not Found", "text/html", "all/error.html");
            }
            System.out.println("-------------------End Request------------");
            clientOutput.flush(); //to clear 'clientOutput'.
            in.close();
            clientOutput.close();
            System.out.println("Client connection closed!");
            System.out.println("Number of requests: " + ++i);
            System.out.println("////////////////////////////\n\n\n");
        }
    }
    private static void sendRespons(OutputStream clientOutput, String status,
     String type, String filePath) throws IOException { //send HTTP response.
        clientOutput.write(("HTTP/1.1 "+ status +"\r\n").getBytes());
        clientOutput.write(("Connection: close\r\n").getBytes());
        clientOutput.write(("Content-type: "+ type +"\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(Files.readAllBytes(Paths.get(filePath))); //write th file.
        clientOutput.write("\r\n".getBytes());
    }
    private static void sortBy(boolean choice) throws IOException { 
        //choice=True --> compare by prices
        //choice=False --> compare by names

        //read items from 'items.txt' file
        BufferedReader itemsReader = 
            new BufferedReader(new FileReader(new File("all/items.txt")));
        String items[] = new String[10];
        int i, j;
        for(i=0; i<10; i++)
            items[i]=itemsReader.readLine();
        itemsReader.close();

        //sorting items 
        String temp;
        for(i=0; i<10; i++)
        {
            for(j=1; j<10; j++)
            {
                if(compare(choice, items[j-1], items[j]))
                {
                    temp=items[j-1];
                    items[j-1]=items[j];
                    items[j]=temp;
                }
            }
        }
        //writ sorted items in 'sort.html' file to use
        try (FileWriter itemsWriter = new FileWriter("all/sort.html")) {
            itemsWriter.write("<ol>");
            for(i=0; i<10; i++) 
                itemsWriter.write("<li>"+ items[i] +"</li>");
            itemsWriter.write("</ol>");
            itemsWriter.close();
        }
    }
    private static boolean compare(boolean choice, String item1, String item2) {
        //choice=True --> compare by prices
        //choice=False --> compare by names
        if(choice) //item.split(" ")[1] used to get the price 
            return Integer.valueOf(item1.split(" ")[1]) 
                < Integer.valueOf(item2.split(" ")[1]);
        return item1.compareTo(item2) > 0;
    }
}  
