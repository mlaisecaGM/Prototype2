public class HelloRunnable implements Runnable {

    private int i = 0;

    public HelloRunnable(int i){
        this.i = i;
    }

    public HelloRunnable(){
        i = 0;
    }
    public void run() {


        System.out.println("Hello from a thread!" + i);
    }

    public static void main(String args[]) {

        //example that shows that because 'i' is printed out of order there is multiple threads at work.
        for(int i = 0; i<10000; i++){
            (new Thread(new HelloRunnable(i))).start();
        }





    }

}