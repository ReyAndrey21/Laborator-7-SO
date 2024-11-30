public class ResourceThread extends Thread{

    private final SharedResource sharedResource;
    private final String color;
    private final int id;

    public ResourceThread(SharedResource sharedResource, String color,int id) {
        this.sharedResource = sharedResource;
        this.color = color;
        this.id = id;
    }

    @Override
    public void run() {
        sharedResource.accessResource(color,id);
    }
}
