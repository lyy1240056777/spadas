package web.exception;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/04/18:19
 **/
public class FileException extends RuntimeException{
    public FileException(String s, Exception ex) {
        super(s, ex);
    }

    public FileException(String s) {
        super(s);
    }
}
