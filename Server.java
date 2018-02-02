//Shubham Patel
//1001376052

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.util.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


/*
 A server that delivers message between pairs of Processes.
 */
public class Server {

  // The server socket.
  private static ServerSocket serverSocket = null;
  // The Process socket.
  private static Socket processSocket = null;

  // This server can accept up to maxProcessesCount Process connections.
  private static int maxProcessesCount;
  private static processThread[] threads;

  public static void main(String args[]) {
	Scanner sc=new Scanner(System.in);
	System.out.println("Enter the number of process:");
	maxProcessesCount=sc.nextInt();
	threads = new processThread[maxProcessesCount];
	
    // The default port number.
	
    int portNumber = 8091;
    if (args.length < 1) {
      System.out.println("Usage: java Server <portNumber>\n"
          + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    /*
     Open a server socket on the portNumber (default 8091). we can't use port less than 1023 if we are not privileged users (root) and 
	 my port 80 is reserved for web so haven't used that either .
     */
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     Creating a Process socket for each connection and pass it to a new Process
     thread.
     */
    while (true) {
      try {
        processSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < maxProcessesCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new processThread(processSocket, threads)).start();
            break;
          }
        }
        if (i == maxProcessesCount) {
          PrintStream os = new PrintStream(processSocket.getOutputStream());
          os.println("Maximum Process Count Reached"); //If we are creating more Processes then we have created at the start on server
          os.close();
          processSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}





/*
This processThread is created for each processes
 */
class processThread extends Thread {

	private int ProcessId;
	private int previousProcessId;
	private int nextProcessId;
	private boolean initiator=false;
	private boolean crashed=false;
	private int coordinator;

	private DataInputStream is = null;
	private PrintStream os = null;
	private Socket processSocket = null;
	private final processThread[] threads;
	private int maxProcessesCount;

  public processThread(Socket processSocket, processThread[] threads) {
    this.processSocket = processSocket;
    this.threads = threads;
    maxProcessesCount = threads.length;
  }
  //suppress warnings for is.readLine() Depricated API
 @SuppressWarnings("deprecation")
  public void run() {
    int maxProcessesCount = this.maxProcessesCount;
    processThread[] threads = this.threads;

    try {
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(processSocket.getInputStream());
      os = new PrintStream(processSocket.getOutputStream());
      String name,rname;
	  name="xyz";
	  rname="xyz";
      while (true) {
        os.println("Enter Process Id:");
        ProcessId = Integer.parseInt(is.readLine().trim());
		os.println("ProcessId:"+ProcessId);
		
		
		break;
		/*
		//Check if names are empty
        if (name.equals("") || rname.equals("")) {
          os.println("The name should not be empty.");
        } else {
          break;
        }*/
		
      }
	  
	  
	  synchronized (this) {
		  if(threads[maxProcessesCount-1]!=null)
		  {
			System.out.println("The current Ring Network is:");
			for (int i = 0; i < maxProcessesCount; i++) {
			  if (threads[i] != null) {
				System.out.print(threads[i].ProcessId);
				if(i<(maxProcessesCount-1))
					System.out.print("-->");
			  }
			}
		}
	  }
	  System.out.println("");
	  os.println("");
	  os.println("Enter initiate if you want to initiate the election");
	  os.println("Enter crash if you want to crash the process");
	  os.println("Enter recover if you want to recover the crashed Process");
	  os.println("");
	  
	  //this loop runs infinitely until user enters exit""
	  //This is to check the user input for different options and take actions accordingly
	  while (true) {
        String line = is.readLine();
        if (line.startsWith("initiate")) //This does two jobs. first is to initiate the election and second is to give user an option to initate when coordinator is crashed.
		{
			if(this.initiator) //This checks who sould initiate when coordinator is crashed
			{
				this.initiator=false;
				this.os.println("");
				this.os.println("Initiating a election because coordinator is crashed.");
				
			}
			if(this.crashed) //This is the check for checking that process can't initiate if it is crashed
			{
				this.os.println("Process is crashed you can't initiate");
			}
			else //This initiates after all the checks are passed
			{
				int count=0;
				for(int i=0;i<maxProcessesCount;i++)
				{
					if(threads[i].crashed)
						count++;
				}
				int arrayCounter=0;
				int tokenArray[]=new int[maxProcessesCount-count];
			for (int i = 0; i < maxProcessesCount; i++) //Outer for loop for each process
			{
				if (threads[i] == this) 
				{
					int k=0;
					int j=0;
					int crashcount=0;
					for(j=i;j<maxProcessesCount-1;j++) //this loop runs from the initiation process to the end of the ring network
					{
						if(crashcount!=0)
						{
							crashcount=0;
							continue;
						}
						int z=j+1;
						while(threads[z+crashcount].crashed) //All the crashcount while loops are to check the number of processes that are crashed from this process
						{
							crashcount++;
							if(z+crashcount==maxProcessesCount)
								z=-1;
						}
						if((j+1+crashcount)<maxProcessesCount)
						{
							tokenArray[arrayCounter]=threads[j].ProcessId;
							arrayCounter++;
							threads[j].os.println("Token "+Arrays.toString(tokenArray)+" sent to Process "+ threads[j+1+crashcount].ProcessId); //Sending message Between Processes
							threads[j].nextProcessId=threads[j+1+crashcount].ProcessId;
							try{
								Thread.sleep(3000); //To give a delay of 3000ms between transmitting tokens
							}
							catch(Exception e)
							{
							}
							threads[j+1+crashcount].os.println("Token "+Arrays.toString(tokenArray)+" Received from Process "+threads[j].ProcessId);//Receiving messages between processes
							threads[j+1+crashcount].previousProcessId=threads[j].ProcessId;
							try{
								Thread.sleep(3000);
							}
							catch(Exception e)
							{
							}	
						}
						else
						{
							tokenArray[arrayCounter]=threads[j].ProcessId;
							arrayCounter++;
							threads[j].os.println("Token "+Arrays.toString(tokenArray)+" sent to Process "+ threads[0+crashcount-1].ProcessId);//Sending message Between Processes
							threads[j].nextProcessId=threads[0+crashcount-1].ProcessId;
							try{
								Thread.sleep(3000);
							}
							catch(Exception e)
							{
							}
							threads[0+crashcount-1].os.println("Token "+Arrays.toString(tokenArray)+" Received from Process "+threads[j].ProcessId);//Receiving messages between processes
							threads[0+crashcount-1].previousProcessId=threads[j].ProcessId;
							try{
								Thread.sleep(3000);
							}
							catch(Exception e)
							{
							}	
						}
					}
					//The code ahead is to send tokens from this last process to first process
						k=j;
						crashcount=0;
						if(threads[k].crashed)
						{
							
						}
						else
						{
							while(threads[0+crashcount].crashed)
							{
								crashcount++;
							}
							tokenArray[arrayCounter]=threads[k].ProcessId;
							arrayCounter++;
								threads[k].os.println("Token "+Arrays.toString(tokenArray)+ "sent to Process "+ threads[0+crashcount].ProcessId);//Sending message Between Processes
								threads[k].nextProcessId=threads[0+crashcount].ProcessId;
							try{
									Thread.sleep(3000);
								}
								catch(Exception e)
								{
								}
							threads[0+crashcount].os.println("Token "+Arrays.toString(tokenArray)+ "Received from Process "+threads[k].ProcessId);//Receiving messages between processes
							threads[0+crashcount].previousProcessId=threads[k].ProcessId;
							try{
									Thread.sleep(3000);
								}
								catch(Exception e)
								{
								}
						}
					for(j=0;j<i;j++) //This is to send tokens from first process to the initiation process
					{
						crashcount=0;
						if(threads[j].crashed)
						{
							continue;
						}
						
						while(threads[j+1+crashcount].crashed)
						{
							crashcount++;
						}
						tokenArray[arrayCounter]=threads[j].ProcessId;
							arrayCounter++;
						threads[j].os.println("Token "+Arrays.toString(tokenArray)+ " sent to Process "+ threads[j+1+crashcount].ProcessId);//Sending message Between Processes
						try{
							Thread.sleep(3000);
						}
						catch(Exception e)
						{
						}
						threads[j+1+crashcount].os.println("Token "+Arrays.toString(tokenArray)+ " Received from Process "+threads[j].ProcessId);//Receiving messages between processes
					try{
							Thread.sleep(3000);
						}
						catch(Exception e)
						{
						}
					}
				}
			}	
			
			
			int maxCoordinator=0;
			for(int i=0;i<tokenArray.length;i++)
			{
				if(tokenArray[i]>maxCoordinator)
					maxCoordinator=tokenArray[i];//to select the highest process as coordinator hence this for loop gets the max of all the processes.
			}
			
			
			System.out.println("Process with process Id: "+maxCoordinator+" is the new Coordinator");
			
			
			//sending the new Coordinator message
			
			for (int i = 0; i < maxProcessesCount; i++)//This for loop iterates only through uncrashed process and send them again the message who is the current coordinator
			{
				threads[i].coordinator=maxCoordinator;
				if (threads[i] == this) 
				{
					int k=0;
					int j=0;
					int crashcount=0;
					for(j=i;j<maxProcessesCount-1;j++)
					{
						if(crashcount!=0)
						{
							crashcount=0;
							continue;
						}
						int z=j+1;
						while(threads[z+crashcount].crashed)
						{
							crashcount++;
							if(z+crashcount==maxProcessesCount)
								z=-1;
						}
						if((j+1+crashcount)<maxProcessesCount)
						{
							threads[j+1+crashcount].os.println("Process with process Id: "+maxCoordinator+" is the new Coordinator");
							try{
								Thread.sleep(3000);
							}
							catch(Exception e)
							{
							}	
						}
						else
						{
							threads[0+crashcount-1].os.println("Process with process Id: "+maxCoordinator+" is the new Coordinator");
							try{
								Thread.sleep(3000);
							}
							catch(Exception e)
							{
							}	
						}
					}
						k=j;
						crashcount=0;
						if(threads[k].crashed)
						{
							
						}
						else
						{
							while(threads[0+crashcount].crashed)
							{
								crashcount++;
							}
							threads[0+crashcount].os.println("Process with process Id: "+maxCoordinator+" is the new Coordinator");
							try{
									Thread.sleep(3000);
								}
								catch(Exception e)
								{
								}
						}
					for(j=0;j<i;j++)
					{
						crashcount=0;
						if(threads[j].crashed)
						{
							continue;
						}
						
						while(threads[j+1+crashcount].crashed)
						{
							crashcount++;
						}
						threads[j+1+crashcount].os.println("Process with process Id: "+maxCoordinator+" is the new Coordinator");
					try{
							Thread.sleep(3000);
						}
						catch(Exception e)
						{
						}
					}
				}
			}
			
			
			
			}
        }else if(line.startsWith("crash")) //This is to crash the current processes for both process and coordinator
		{
			this.crashed=true;
			if(this.coordinator==this.ProcessId)
			{
				this.os.println("Coordinator is crashed");
				for(int i=0;i<maxProcessesCount;i++)
				{
					if(threads[i].ProcessId==this.nextProcessId)
					{
						threads[i].initiator=true;
						threads[i].os.println("Please Initiate the election because coordinator has crashed");
					}
				}
			}
			else{
				this.os.println("Process is crashed");
			}
		}
		else if(line.startsWith("recover")) //This is to recover the crashed Process
		{
			this.crashed=false;
			this.os.println("Process is recovered");
		}
		else if(line.startsWith("quit")) //This is to quit the execution
		{
			break;
		}
		else //This checks whether user entered invalid input
		{
			this.os.println("Invalid Input");
		}
        
      }
	  
      /*
       Close the output stream, close the input stream, close the socket.
       */
      is.close();
      os.close();
      processSocket.close();
    } catch (IOException e) {
    }
  }
}





