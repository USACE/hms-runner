package usace.cc.plugin.hmsrunner;

public class Block {
    //represents a block starting with a keyword: and ending with END: in a grid or met file.
    public String[] Lines;
    public Block(String[] lines){
        Lines = lines;
    }
}
