//Fix all created methods to make sure they can handle .. issues like CD does
//512 issue




import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class fat32_reader {

    static short BPB_BytesPerSec, BPB_RsvdSecCnt;
    static int BPB_SecPerClus, BPB_NumFATS, BPB_FATSz32, FirstDataSector, BPB_RootClus;
    static int BytesPerSectorPerCluster;
    static byte[] myByteArray;
    static LinkedList<Integer> pathClusters = new LinkedList<>();
    static LinkedList<String> pathNames = new LinkedList<>();
    static boolean running;
    

    public static void main(String[] args) throws IOException {
        /*
        args[0] will be the .img
        Sooo, what to do with that
         */
        File file = new File(args[0]);
        String filePath = file.getAbsolutePath();
        Path path = Paths.get(filePath);
        try {
            myByteArray = Files.readAllBytes(path);
        } catch (Exception e) {
            System.out.println("File Error");
            System.exit(0);
        }

        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.put(myByteArray[12]);
        bb.put(myByteArray[11]);
        bb.rewind();
        BPB_BytesPerSec = bb.getShort();

        BPB_SecPerClus = myByteArray[13];

        BytesPerSectorPerCluster = BPB_BytesPerSec*BPB_SecPerClus;

        bb = ByteBuffer.allocate(2);
        bb.put(myByteArray[15]);
        bb.put(myByteArray[14]);
        bb.rewind();
        BPB_RsvdSecCnt = bb.getShort();

        BPB_NumFATS = myByteArray[16];

        bb = ByteBuffer.allocate(4);
        bb.put(myByteArray[39]);
        bb.put(myByteArray[38]);
        bb.put(myByteArray[37]);
        bb.put(myByteArray[36]);
        bb.rewind();
        BPB_FATSz32 = bb.getInt();

        bb = ByteBuffer.allocate(4);
        bb.put(myByteArray[47]);
        bb.put(myByteArray[46]);
        bb.put(myByteArray[45]);
        bb.put(myByteArray[44]);
        bb.rewind();
        BPB_RootClus = bb.getInt();
        pathClusters.add(BPB_RootClus);

        FirstDataSector = BPB_RsvdSecCnt + (BPB_NumFATS * BPB_FATSz32);

    
//        System.out.println(file.getPath());
//        System.out.println(file.getAbsolutePath());
        running = true;
        Scanner scanner = new Scanner(System.in); 



        while(running){
            if (!pathNames.isEmpty()) {
                System.out.print(pathNames.getLast());
            }
            System.out.print("] ");
            String input = scanner.nextLine(); 
            String[] arguments = input.split(" ");
            if (arguments[0].equals("stop")) stop();
            else if (arguments[0].equals("info")) info();
            else if (arguments[0].equals("ls")) ls(arguments, pathClusters.getLast());
            else if (arguments[0].equals("cd")) cd(arguments, pathClusters.getLast());
            else if (arguments[0].equals("stat")) stat(arguments);
            else if (arguments[0].equals("size")) size(arguments);
            else if (arguments[0].equals("read")) read(arguments);
            else{
                System.out.println("Error: Not a valid command");
            }


        }
        scanner.close();
    }

    /**
     * stop
     *
     * Description: exits your shell-like utility
     */
    public static void stop(){
        running = false;
    }

    /**
     * info
     *
     * Description: prints out information about the following fields in both hex and base 10.
     * Be careful to use the proper endian-ness:
     *    o BPB_BytesPerSec
     *    o BPB_SecPerClus
     *    o BPB_RsvdSecCnt
     *    o BPB_NumFATS
     *    o BPB_FATSz32
     *
     * Sample execution:
     *    /] info
     *    BPB_BytesPerSec is 0x200, 512
     *    BPB_SecPerClus is 0x1, 1
     *    BPB_RsvdSecCnt is 0x20, 32
     *    BPB_NumFATs is 0x2, 2
     *    BPB_FATSz32 is 0x3f1, 1009
     *    /]
     * Note: Do not assume and hard-code these values into your reader!
     * Your reader will be tested with images with different sector sizes,
     * different number of sectors per cluster, etc.,
     * and everything should still work correctly.
     */
    public static void info(){
        System.out.println("BPB_BytesPerSec is 0x" + Integer.toHexString(BPB_BytesPerSec) + ", " + BPB_BytesPerSec);
        System.out.println("BPB_SecPerClus is 0x" + Integer.toHexString(BPB_SecPerClus) + ", " + BPB_SecPerClus);
        System.out.println("BPB_RsvdSecCnt is 0x" + Integer.toHexString(BPB_RsvdSecCnt) + ", " + BPB_RsvdSecCnt);
        System.out.println("BPB_NumFATS is 0x" + Integer.toHexString(BPB_NumFATS) + ", " + BPB_NumFATS);
        System.out.println("BPB_FATSz32 is 0x" + Integer.toHexString(BPB_FATSz32) + ", " + BPB_FATSz32);
    }

    /**
     * ls <DIR_NAME>
     *
     * Description: For the directory at the relative or absolute path specified in DIR_NAME,
     * lists the contents of DIR_NAME,
     * including “.” and “..”, and including hidden files
     * (in other words, it should behave like the real “ls -a”).
     * Display an error message if DIR_NAME is not a directory.
     * Like the “real” ls -a, "." and ".." are shown for all directories,
     * even the root directory (despite the fact that “..” is meaningless for the root directory).
     * Like the “real” ls -a, your output should be alphabetically sorted
     * Both of
     * these should work:
     * ls DIR (relative path from where we are (the root directory))
     * ls /DIR (absolute path) Sample execution:
     * /] ls .
     * . .. A B C D E F G H I J K L M N O P Q R S T U V W X Y Z /]
     *    /] ls bob.txt
     *    Error: bob.txt is not a directory
     *    /]
     */
    public static void ls(String[] arguments, Integer CurrentCluster){
        if (arguments.length < 2) {
            System.out.println("Error: Not enough arguments");
            return;
        } else if (arguments.length > 2) {
            System.out.println("Error: Too many arguments");
            return;
        } else if (arguments[1].equals(".")) {
            listFilesFromCluster(pathClusters.getLast(), CurrentCluster);
        }  else if (arguments[1].equals("..")) {
            if (pathClusters.size() == 1) {
                System.out.println("Error: .. is not a directory");
            } else {
                listFilesFromCluster(pathClusters.get(pathClusters.size()-2), pathClusters.get(pathClusters.size()-2));
            }
        } else {
            if (arguments[1].toCharArray()[0] != '/') arguments[1] = "/" + arguments[1];

            String argument = arguments[1];
            if (argument.toCharArray()[0] != '/') argument = "/" + argument;
            String[] paths = argument.split("/");
    
            if (paths.length == 0) {
                System.out.println("Error " + arguments[1] + " is not a directory"); 
                return;
            }
    
            List<Integer> pathList = new ArrayList<Integer>();
            pathList.addAll(pathClusters);

    
            for (int i = 1; i < paths.length; i++) {
                if (paths[i].equals(".")) continue;
                else if (paths[i].equals("..")) {
                    if (pathList.size() == 0) {
                        System.out.println("Error " + arguments[1] + " is not a directory");
                        return;
                    } else pathList.remove(pathList.size()-1);
                }
                else {
                    int toAdd = clusterOfFile(paths[i], pathList.get(pathList.size()-1));
                    if (toAdd < 0) {
                        System.out.println("Error " + arguments[1] + " is not a directory"); 
                        return;
                    }
                    pathList.add(toAdd);
                }
            }

                String[] newArguments = new String[]{"","."};
                ls(newArguments, pathList.get(pathList.size()-1));
            
        } /* else {
            
            try {
                listFilesFromCluster(clusterOfFile(arguments[1], pathClusters.getLast()), pathClusters.getLast());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + arguments[1] + " is not a directory");
            }
        } */

    }

    private static void listFilesFromCluster(int clusterNumber, int CurrentCluster) throws IllegalArgumentException {

        if (clusterNumber < 0) {
            throw new IllegalArgumentException();
        }

        List<String> directories = new ArrayList<String>();
       
            //Read 32 bits at a time
            int clusterStart = (((CurrentCluster-2)*BPB_BytesPerSec) + (FirstDataSector * BPB_BytesPerSec));
            int b = 0;
            
            boolean continueReading = true;
            while(continueReading) {
                b = 0;
                if (CurrentCluster == 2) b+=64; //skip weirdt hing
                while(b < BytesPerSectorPerCluster) {

                    if (myByteArray[clusterStart + b] == 0) break;    

                    if (myByteArray[clusterStart + b] == 95) {
                    break;
                    }  


                    if (myByteArray[clusterStart + b] == -27) {

                        b+= 32;
                        continue;
                    }

                    if (myByteArray[clusterStart + b + 11] == 15) {
                        b+=32;
                        continue;
                    }
                
                    String toAdd = "";
        
                    for (int i = 0; i < 8; i++) {
                        char c = (char) myByteArray[clusterStart + b + i];
                        if (c == 32) break;
                        toAdd += c;
                    }
                    b+=8;
                    if (myByteArray[clusterStart + b] != 32) {
                        toAdd+=".";
                        for (int i = 0; i < 3; i++) {
                            toAdd += (char) myByteArray[clusterStart + b+i];
                        }
                    }
                    b+=3;
                    b+=21;
                    directories.add(toAdd);
                }

                int tableEntryForCluster = (BPB_RsvdSecCnt * BPB_BytesPerSec + (CurrentCluster * 4));
                ByteBuffer bb = ByteBuffer.allocate(4);
                bb.put(myByteArray[tableEntryForCluster + 3]);
                bb.put(myByteArray[tableEntryForCluster + 2]);
                bb.put(myByteArray[tableEntryForCluster + 1]);
                bb.put(myByteArray[tableEntryForCluster]);
                bb.rewind();
                CurrentCluster = bb.getInt();


                if (CurrentCluster >= 268435448 && CurrentCluster <= 268435455) {
                    continueReading = false;
                } else {
                    clusterStart = ((CurrentCluster-2)*BPB_BytesPerSec) + (FirstDataSector * BPB_BytesPerSec);
                }  
            }

            if (!directories.contains(".")) directories.add(".");
            if (!directories.contains("..")) directories.add("..");

            Collections.sort(directories);
            for (int i = 0; i < directories.size() - 1; i++) {
                System.out.print(directories.get(i) + " ");
            }
            System.out.println(directories.get(directories.size()-1));

    }

    private static int clusterOfFile(String fileName, Integer CurrentCluster) {
        int clusterStart = ((CurrentCluster-2)*BPB_BytesPerSec) + (FirstDataSector * BPB_BytesPerSec);
        int b = 0;
        boolean continueReading = true;
        while(continueReading) {
            b = 0;
            if (CurrentCluster == 2) b+=64; //skip weirt hing
            while(b < BytesPerSectorPerCluster) {

                if (myByteArray[clusterStart + b] == 0) break;    

                if (myByteArray[clusterStart + b] == 95) {
                   break;
                } 

                if (myByteArray[clusterStart + b] == -27) {
                   

                    b+= 32;
                    continue;
                }
                if (myByteArray[clusterStart + b + 11] == 15) {
                    b+=32;
                    continue;
                }
                String string = "";

                for (int i = 0; i < 8; i++) {
                    char c = (char) myByteArray[clusterStart + b + i];
                    if (c == 32) break;
                    string += c;
                }
                b+=8;
                if (myByteArray[clusterStart + b] != 32) {
                    string+=".";
                    for (int i = 0; i < 3; i++) {
                        string += (char) myByteArray[clusterStart + b+i];
                    }
                }
                if (fileName.toUpperCase().equals(string)) {
                    b-=8;
                    ByteBuffer bb = ByteBuffer.allocate(4);
                    bb.put(myByteArray[clusterStart + b + 21]);
                    bb.put(myByteArray[clusterStart + b + 20]);
                    bb.put(myByteArray[clusterStart + b + 27]);
                    bb.put(myByteArray[clusterStart + b + 26]);
                    bb.rewind();
                    return bb.getInt(); 
                } else {
                    b+=24;
                }

            }
            if (!continueReading) break;
            int tableEntryForCluster = (BPB_RsvdSecCnt * BPB_BytesPerSec + (CurrentCluster * 4));
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.put(myByteArray[tableEntryForCluster + 3]);
            bb.put(myByteArray[tableEntryForCluster + 2]);
            bb.put(myByteArray[tableEntryForCluster + 1]);
            bb.put(myByteArray[tableEntryForCluster]);
            bb.rewind();
            CurrentCluster = bb.getInt();

            if (CurrentCluster >= 268435448 && CurrentCluster <= 268435455) {
                continueReading = false;
            } else {
                clusterStart = ((CurrentCluster-2)*BPB_BytesPerSec) + (FirstDataSector * BPB_BytesPerSec);
            }  

        } 
        return -1;
    }

    private static String nameOfFile(String fileName) {

        int CurrentCluster = pathClusters.getLast();
        int clusterStart = ((CurrentCluster-2)*BPB_BytesPerSec) + (FirstDataSector * BPB_BytesPerSec);
        int b = 0;
        boolean continueReading = true;
        while(continueReading) {
            b = 0;
            if (CurrentCluster == 2) b+=64; //skip weirt hing
            while(b < BytesPerSectorPerCluster) {

                if (myByteArray[clusterStart + b] == 0) break;    

                if (myByteArray[clusterStart + b] == 95) {
                   break;
                }  

                if (myByteArray[clusterStart + b] == -27) {

                    b+= 32;
                    continue;
                }
                if (myByteArray[clusterStart + b + 11] == 15) {
                    b+=32;
                    continue;
                }
                String string = "";

                for (int i = 0; i < 8; i++) {
                    char c = (char) myByteArray[clusterStart + b + i];
                    if (c == 32) break;
                    string += c;
                }
                b+=8;
                if (myByteArray[clusterStart + b] != 32) {
                    string+=".";
                    for (int i = 0; i < 3; i++) {
                        string += (char) myByteArray[clusterStart + b+i];
                    }
                }
                if (fileName.toUpperCase().equals(string)) {
                    return string;                   
                } else {
                    b+=24;
                }

            }
            if (!continueReading) break;
            int tableEntryForCluster = (BPB_RsvdSecCnt * BPB_BytesPerSec + (CurrentCluster * 4));
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.put(myByteArray[tableEntryForCluster + 3]);
            bb.put(myByteArray[tableEntryForCluster + 2]);
            bb.put(myByteArray[tableEntryForCluster + 1]);
            bb.put(myByteArray[tableEntryForCluster]);
            bb.rewind();
            CurrentCluster = bb.getInt();

            if (CurrentCluster >= 268435448 && CurrentCluster <= 268435455) {
                continueReading = false;
            } else {
                clusterStart = ((CurrentCluster-2)*BPB_BytesPerSec) + (FirstDataSector * BPB_BytesPerSec);
            }  

        } 
        return "";
    }

    private static boolean checkPath(String path, Integer CurrentCluster) {
        String[] splitPath = path.split("/");
        int cluster = clusterOfFile(splitPath[1], CurrentCluster);
        if (cluster < 0) {
            return false;
        } else if (splitPath.length == 2 || splitPath.length == 1) {
            return true;
        } else {
            String newPath = "";
            for (int i = 2; i < splitPath.length; i++) {
                newPath += "/";
                newPath += splitPath[i];
            }
            return checkPath(newPath, cluster);
        }
    }

    private static Integer directoryEntryInCluster(String fileName, Integer CurrentCluster) {
        int clusterStart = ((CurrentCluster-2)*BPB_BytesPerSec) + (FirstDataSector * BPB_BytesPerSec);
        int b = 0;

        boolean continueReading = true;
        while(continueReading) {   
            b = 0;
            while(b < BytesPerSectorPerCluster) {

                if (myByteArray[clusterStart + b] == 0) break;    

                if (myByteArray[clusterStart + b] == 95) {
                    break;
                }  

                if (myByteArray[clusterStart + b] == -27) {
                    b+= 32;
                    continue;
                }
                
                if (myByteArray[clusterStart + b + 11] == 15) {
                    b+=32;
                    continue;
                }
                String string = "";

                for (int i = 0; i < 8; i++) {
                    char c = (char) myByteArray[clusterStart + b + i];
                    if (c == 32) break;
                    string += c;
                }
                b+=8;
                if (myByteArray[clusterStart + b] != 32) {
                    string+=".";
                    for (int i = 0; i < 3; i++) {
                        string += (char) myByteArray[clusterStart + b+i];
                    }
                }
                if (fileName.toUpperCase().equals(string.toUpperCase())) {
                    b-=8;
                    ByteBuffer bb = ByteBuffer.allocate(4);
                    bb.put(myByteArray[clusterStart + b + 21]);
                    bb.put(myByteArray[clusterStart + b + 20]);
                    bb.put(myByteArray[clusterStart + b + 27]);
                    bb.put(myByteArray[clusterStart + b + 26]);
                    bb.rewind();
                    return clusterStart + b; 

                } else {
                    b+=24;
                }
            }

          
            if (!continueReading) break;
            int tableEntryForCluster = (BPB_RsvdSecCnt * BPB_BytesPerSec + (CurrentCluster * 4));
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.put(myByteArray[tableEntryForCluster + 3]);
            bb.put(myByteArray[tableEntryForCluster + 2]);
            bb.put(myByteArray[tableEntryForCluster + 1]);
            bb.put(myByteArray[tableEntryForCluster]);
            bb.rewind();
            CurrentCluster = bb.getInt();

            if (CurrentCluster >= 268435448 && CurrentCluster <= 268435455) {
                continueReading = false;
            } else {
                clusterStart = ((CurrentCluster-2)*BPB_BytesPerSec) + (FirstDataSector * BPB_BytesPerSec);
            }  

        }
        return -1;
    } 

    /**
     * stat <FILE_NAME/DIR_NAME>
     *
     * Description: For the file or directory at the relative or absolute path
     * specified in FILE_NAME or DIR_NAME, prints the size of the file or directory,
     * the attributes of the file or directory,
     * and the first cluster number of the file or directory.
     * Return an error if FILE_NAME/DIR_NAME does not exist (see example below).
     * (Note: The size of a directory will always be zero.)
     * If a file has more than one Attributes,
     * print them space delimited,
     * in descending order of the bit value of the attributes.
     * Sample execution:
     * Directory or file exists:
     *    /] stat CONST.TXT
     *    Size is 45119
     *    Attributes ATTR_ARCHIVE
     *    Next cluster number is 0x0004
     *    /]
     * Directory or file doesn’t exist:
     *    /] stat NOTHERE.TXT
     *    Error: file/directory does not exist
     *    /]
     */
    public static void stat(String[] arguments) {
        if (arguments.length < 2) {
            System.out.println("Error: Not enough arguments");
            return;
        } else if (arguments.length > 2) {
            System.out.println("Error: Too many arguments");
            return;
        }

        String argument = arguments[1];
        if (argument.toCharArray()[0] != '/') argument = "/" + argument;
        String[] paths = argument.split("/");

        if (paths.length == 0) {
            System.out.println("Error: file/directory does not exist");
            return;
        }

        List<Integer> pathList = new ArrayList<Integer>();
        pathList.addAll(pathClusters);


        for (int i = 1; i < paths.length-1; i++) {
            if (paths[i].equals(".")) continue;
            else if (paths[i].equals("..")) {
                if (pathList.size() == 0) {
                    System.out.println("Error: file/directory does not exist");
                    return;
                } else pathList.remove(pathList.size()-1);
            }
            else {
                int toAdd = clusterOfFile(paths[i], pathList.get(pathList.size()-1));
                if (toAdd < 0) {
                    System.out.println("Error " + arguments[1] + " is not a file"); 
                    return;
                }
                pathList.add(toAdd);
            }
        }


        int fileStart = directoryEntryInCluster(paths[paths.length-1], pathList.get(pathList.size()-1));
        if (fileStart < 0) {
            System.out.println("Error: file/directory does not exist");
            return;
        }

        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.put(myByteArray[fileStart + 31]);
        bb.put(myByteArray[fileStart + 30]);
        bb.put(myByteArray[fileStart + 29]);
        bb.put(myByteArray[fileStart + 28]);
        bb.rewind();

        System.out.println("Size is " + bb.getInt());

        int attribute = myByteArray[fileStart + 11];
        if (attribute == 16) {
            System.out.println("Attributes: ATTR_DIRECTORY"); 
        } else if (attribute == 32) {
            System.out.println("Attributes: ATTR_ARCHIVE"); 
        } else if (attribute == 1) {
            System.out.println("Attributes: ATTR_READ_ONLY"); 
        } else if (attribute == 2) {
            System.out.println("Attributes: ATTR_HIDDEN"); 
        } else if (attribute == 4) {
            System.out.println("Attributes: ATTR_SYSTEM"); 
        } else if (attribute == 8) {
            System.out.println("Attributes: ATTR_VOLUME_ID"); 

        }

        System.out.println("Next cluster number is 0x" + Integer.toHexString(clusterOfFile(paths[paths.length-1], pathList.get(pathList.size()-1))));
    
        
    } 
    
    
    /**
     * size <FILE_NAME>
     *
     * Description: For the file at the relative or absolute path specified in FILE_NAME, prints the size of file.
     * Return an error if FILE_NAME does not exist or is not a file. Sample execution:
     *    /] size FOLDER1/FILE.TXT
     *    Size of FOLDER1/FILE.TXT is 42376 bytes
     *    /]
     *    /] size /DIR/NOT_HERE.TXT
     *    Error: /DIR/NOT_HERE.TXT is not a file
     *    /]
     *    /] size FOLDER1
     *    Error: FOLDER1 is not a file
     *    /]
     */

    public static int sizeNumber(String[] arguments) {
       
        String argument = arguments[1];
        if (argument.toCharArray()[0] != '/') argument = "/" + argument;
        String[] paths = argument.split("/");

        List<Integer> pathList = new ArrayList<Integer>();
        pathList.addAll(pathClusters);

        for (int i = 1; i < paths.length-1; i++) {
            if (paths[i].equals(".")) continue;
            else if (paths[i].equals("..")) {
                pathList.remove(pathList.size()-1);
            } else {
                int toAdd = clusterOfFile(paths[i], pathList.get(pathList.size()-1));
                pathList.add(toAdd);
            }
            
        }

        int fileStart = directoryEntryInCluster(paths[paths.length-1], pathList.get(pathList.size()-1));

        if (fileStart < 0) {
            return -1;
        }
        
        int attribute = myByteArray[fileStart + 11];
        if (attribute == 16) {
            return -1;
        } else {
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.put(myByteArray[fileStart + 31]);
            bb.put(myByteArray[fileStart + 30]);
            bb.put(myByteArray[fileStart + 29]);
            bb.put(myByteArray[fileStart + 28]);
            bb.rewind();
            int size = bb.getInt();
            return size;
        }

    }

    public static void size(String[] arguments){
        if (arguments.length < 2) {
            System.out.println("Error: Not enough arguments");
            return;
        } else if (arguments.length > 2) {
            System.out.println("Error: Too many arguments");
            return;
        }

        String argument = arguments[1];
        if (argument.toCharArray()[0] != '/') argument = "/" + argument;
        String[] paths = argument.split("/");

        if (paths.length == 0) {
            System.out.println("Error " + arguments[1] + " is not a file"); 
            return;
        }

        List<Integer> pathList = new ArrayList<Integer>();
        pathList.addAll(pathClusters);

        for (int i = 1; i < paths.length-1; i++) {
            if (paths[i].equals(".")) continue;
            else if (paths[i].equals("..")) {
                if (pathList.size() == 0) {
                    System.out.println("Error " + arguments[1] + " is not a file"); 
                    return;
                } else pathList.remove(pathList.size()-1);
            }
            else {
                int toAdd = clusterOfFile(paths[i], pathList.get(pathList.size()-1));
                if (toAdd < 0) {
                    System.out.println("Error " + arguments[1] + " is not a file"); 
                    return;
                }
                pathList.add(toAdd);
            }
            
        }

        int fileStart = directoryEntryInCluster(paths[paths.length-1], pathList.get(pathList.size()-1));

        if (fileStart < 0) {
            System.out.println("Error " + arguments[1] + " is not a file"); 
            return;
        }
        
        int attribute = myByteArray[fileStart + 11];
        if (attribute == 16) {
            System.out.println("Error: " + arguments[1] + " is not a file"); 
            return;
        } else {
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.put(myByteArray[fileStart + 31]);
            bb.put(myByteArray[fileStart + 30]);
            bb.put(myByteArray[fileStart + 29]);
            bb.put(myByteArray[fileStart + 28]);
            bb.rewind();
            int size = bb.getInt();
            System.out.println("Size of " + arguments[1] + " is " + size + " bytes");
        }
      
    }

    /**
     * cd <DIR_NAME>
     * Description: For the directory at the relative or absolute path specified in DIR_NAME,
     * changes the current directory to DIR_NAME.
     * The prompt is updated to show the new current directory.
     * Return an error if DIR_NAME does not exist or is not a directory.
     * Sample execution:
     *    /] cd FOLDER1
     *    /FOLDER1]
     *    /FOLDER1] cd /FOLDER2
     *    /FOLDER2]
     *    /] cd /FOLDER1/FILE.TXT
     *    Error: /FOLDER1/FILE.TXT is not a directory
     *    /]
     *    /] cd MSNGFLDR
     *    Error: MSNGFLDR is not a directory
     *    /]
     *
     */
    public static void cd(String[] arguments, Integer CurrentCluster) {
        if (arguments.length < 2) {
            System.out.println("Error: Not enough arguments");
            return;
        } else if (arguments.length > 2) {
            System.out.println("Error: Too many arguments");
            return;
        }

        String argument = arguments[1];

        String[] paths = argument.split("/");
        if (argument.toCharArray()[0] == '/') {
            boolean exists = checkPath(argument, pathClusters.getLast());
            exists = true;
            if (!exists) {
                System.out.println("Error: " + argument + " is not a directory");
                return;
            } else {
                for (int i = 1; i < paths.length; i++) {
                    String[] newArgument = new String[]{"cd",paths[i]};
                    cd(newArgument, pathClusters.getLast());
                }
            }
        } else if (paths.length > 1) {
            String[] newArgument = new String[]{"cd","/" + arguments[1]};
            cd(newArgument, CurrentCluster);
        } else if (argument.equals(".")) {
            return;
        } else if (argument.equals("..")) {
            if (pathNames.isEmpty()) {
                System.out.println("Error");
            } else {
                pathNames.removeLast();
                pathClusters.removeLast();
            }
        } else {
            int cluster = clusterOfFile(argument, pathClusters.getLast());
            if (cluster < 0) {
                System.out.println(argument + " is not a directory");
                return;
            }
            pathNames.add(nameOfFile(argument));
            pathClusters.add(cluster);
        }

    }
    
    /**
     * read <FILE_NAME> <OFFSET> <NUMBYTES>
     *
     *
     *       Description: For the file at the relative or absolute path specified in FILE_NAME,
     *       reads from the file starting OFFSET bytes from the beginning of the file
     *       and prints NUM_BYTES bytes of the file’s contents,
     *       interpreted asASCIItext(foreachbyte,ifthebyteislessthandecimal127,printthecorrespondingasciicharacter. Else, print " 0xNN ", where NN is the hex value of the byte).
     *
     *       Return an error when trying to read an unopened file, a nonexistent file, or read data outside the file. Sample execution:
     *
     *       Successful read
     *       /] read CONST.TXT 0 15
     *       Provided by USC
     *       /]
     *
     *
     *
     *       Unsuccessful reads
     *       /] read 10BYTES.TXT 5 5
     *       Error: attempt to read data outside of file bounds /]
     *       /] read 10BYTES.TXT -1 5
     *       Error: OFFSET must be a positive value /]
     *       /] read 10BYTES.TXT 1 -5
     *       Error: NUM_BYTES must be a greater than zero /]
     *       /] read 10BYTES.TXT 1 0
     *       Error: NUM_BYTES must be a greater than zero /]
     *       /] read NOTOPEN.TXT 5 5
     *       Error: file is not open
     *      /]
     *      /] read FOLDER1 5 5
     *      Error: FOLDER1 is not a file
     *      /]
     *      /] read /DIR/NOT_HERE.TXT 5 5
     *      Error: /DIR/NOT_HERE.TXT is not a file
     *      /]
     */
    public static void read(String[] arguments) {
        if (arguments.length < 4) {
            System.out.println("Error: Not enough arguments");
            return;
        } else if (arguments.length > 4) {
            System.out.println("Error: Too many arguments");
            return;
        } 

        Integer OFFSET, NUM_BYTES;

        try {
            OFFSET = Integer.parseInt(arguments[2]);
            NUM_BYTES = Integer.parseInt(arguments[3]);
        } catch (NumberFormatException e) {
            System.out.println("Error");
            return;
        }

        if (OFFSET < 0) {
            System.out.println("Error: OFFSET must be a positive value");
            return;
        } else if (NUM_BYTES < 0) {
            System.out.println("Error: NUM_BYTES must be a greater than zero");
            return;
        }

        String argument = arguments[1];
        if (argument.toCharArray()[0] != '/') argument = "/" + argument;
        String[] paths = argument.split("/");


        List<Integer> pathList = new ArrayList<Integer>();
        pathList.addAll(pathClusters);


        for (int i = 1; i < paths.length-1; i++) {
            if (paths[i].equals(".")) continue;
            else if (paths[i].equals("..")) {
                if (pathList.size() == 0) {
                    System.out.println("Error " + arguments[1] + " is not a file");
                    return;
                } else pathList.remove(pathList.size()-1);
            }
            else {
                int toAdd = clusterOfFile(paths[i], pathList.get(pathList.size()-1));
                if (toAdd < 0) {
                    System.out.println("Error " + arguments[1] + " is not a file"); 
                    return;
                }
                pathList.add(toAdd);
            }
        }

        int fileEntry = directoryEntryInCluster(paths[paths.length-1], pathList.get(pathList.size()-1));

        if (fileEntry < 0) {
            System.out.println("Error " + arguments[1] + " is not a file"); 
            return;
        }
        
        int attribute = myByteArray[fileEntry + 11];
        if (attribute == 16) {
            System.out.println("Error: " + arguments[1] + " is not a file"); 
            return;
        }

        int cluster = clusterOfFile(paths[paths.length-1],  pathList.get(pathList.size()-1));

       int numOfClustersForFile = 1;

        boolean continueReading = true;
        int c = cluster;

        while(continueReading) {
        
            int tableEntryForCluster = (BPB_RsvdSecCnt * BPB_BytesPerSec + (c * 4));
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.put(myByteArray[tableEntryForCluster + 3]);
            bb.put(myByteArray[tableEntryForCluster + 2]);
            bb.put(myByteArray[tableEntryForCluster + 1]);
            bb.put(myByteArray[tableEntryForCluster]);
            bb.rewind();
            c = bb.getInt();

            if (c >= 268435448 && c <= 268435455) {
                continueReading = false;
            } else {
                numOfClustersForFile++;
            }
            
        }

        if (OFFSET + NUM_BYTES > sizeNumber(arguments)) {
            System.out.println("Error: attempt to read data outside of file bounds");
            return;
        } 





        int n = OFFSET / BytesPerSectorPerCluster;
        int b = OFFSET % BytesPerSectorPerCluster;

        int readStart = ((cluster-2)*BPB_BytesPerSec) + (FirstDataSector * BPB_BytesPerSec);
        for (int i = 1; i < n; i++) {
            int tableEntryForCluster = (BPB_RsvdSecCnt * BPB_BytesPerSec + (cluster * 4));
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.put(myByteArray[tableEntryForCluster + 3]);
            bb.put(myByteArray[tableEntryForCluster + 2]);
            bb.put(myByteArray[tableEntryForCluster + 1]);
            bb.put(myByteArray[tableEntryForCluster]);
            bb.rewind();
            cluster = bb.getInt();

            if (cluster >= 268435448 && cluster <= 268435455) {
                System.out.println("Error: attempt to read data outside of file bounds");
                return;
            } else {
                readStart = ((cluster-2)*BPB_BytesPerSec) + (FirstDataSector * BPB_BytesPerSec);
            }
        }

        continueReading = true;
        int count = 0;
        while(continueReading) {
            while (b < BytesPerSectorPerCluster) {
                int i = myByteArray[readStart + b];
                if (i < 127) {
                    System.out.print((char) i);
                } else {
                    System.out.print("0x" + Integer.toHexString(i));
                }
                b++;
                count++;
                if (count == NUM_BYTES) {
                    System.out.println();
                    return;
                }
            }
            b = 0;

            int tableEntryForCluster = (BPB_RsvdSecCnt * BPB_BytesPerSec + (cluster * 4));
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.put(myByteArray[tableEntryForCluster + 3]);
            bb.put(myByteArray[tableEntryForCluster + 2]);
            bb.put(myByteArray[tableEntryForCluster + 1]);
            bb.put(myByteArray[tableEntryForCluster]);
            bb.rewind();
            cluster = bb.getInt();

            if (cluster >= 268435448 && cluster <= 268435455) {
                continueReading = false;
            } else {
                readStart = ((cluster-2)*BPB_BytesPerSec) + (FirstDataSector * BPB_BytesPerSec);

            }  

        }

    }
    

}


