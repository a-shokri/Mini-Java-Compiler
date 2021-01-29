package members;

public class Counter {

    private static Integer count = 0;
    public static Counter counter;

    private Counter() {
    }

    public static Counter getInstance() {

        if (counter == null) {

            counter = new Counter();
            return counter;
        }

        return counter;

    }

    public int getCount() {
        return ++count;
    }

}
