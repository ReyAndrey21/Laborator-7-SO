public class Main {
    public static void main(String[] args) {
        SharedResource sharedResource = new SharedResource();

        for (int i = 0; i < 5; i++) {
            ResourceThread t1=new ResourceThread(sharedResource,"White", i);
            ResourceThread t2=new ResourceThread(sharedResource,"Black", i);

            t1.start();
            t2.start();
        }

    }
}
