import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


public class fat32_reader {

    public static void main(String[] args) throws IOException {
        /*
        args[0] will be the .img
        Sooo, what to do with that
         */
        File file = new File(args[0]);
        String filePath = file.getAbsolutePath();
        Path path = Paths.get(filePath);
        byte[] myByteArray = Files.readAllBytes(path);
        System.out.println(myByteArray[0]);

//        System.out.println(file.getPath());
//        System.out.println(file.getAbsolutePath());





        //while(true){
            /*
            Scan for input from the user
             */
       // }
    }




    /**
     * stop
     *
     * Description: exits your shell-like utility
     */
    public void stop(){

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
    public void info(){

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
    public void ls(){

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
    public void stat(){

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
    public void size(){

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
    public void cd(){

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
    public void read(){

    }







}
