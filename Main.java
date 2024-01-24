import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
class Admin
{
    int ID;
    String pass;
    ChargingStation cs;
   public Admin(int ID,String pass, ChargingStation cs)
   {
    this.ID=ID;
    this.pass=pass;
    this.cs=cs;
   }

}
class ChargingStation {
    private int id;
    int location;
    private final int totalSlots;
    private int availableSlots;
    private final List<Car> waitingList;
    public FileWriter f;

    public ChargingStation(int id, int location, int totalSlots, FileWriter f) {
        this.id = id;
        this.location = location;
        this.totalSlots = totalSlots;
        this.availableSlots = totalSlots;
        this.waitingList = new ArrayList<>();
        this.f=f;
    }

    public synchronized boolean bookslot(Car car, int duration) {
        if (availableSlots > 0) {
            System.out.println(car.ID() + " will be charging for " + duration + " Minutes in Station"+id);
            try {
                f.write(car.ID() + " Charged for " + duration + " Minutes in Station"+id+"\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            availableSlots--;
            return true;
        } else {
            System.out.println(car.ID() + " is added to the waiting list.");
            try {
                f.write(car.ID() + " is added to the waiting list.\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (!waitingList.contains(car)) {
                waitingList.add(car);
            }
            try {
                wait(); // Car is added to the waiting list
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false; // Room booking failed
        }
    }

    public synchronized void releaseSlot(Car car) {
        availableSlots++;
        System.out.println(car.ID()+" Charging slot released. Available slots: " + availableSlots);
         try {
                f.write(car.ID()+" Charging slot released. Available slots: " + availableSlots+"\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        checkWaitingList();
        notify(); // Notify waiting cars about the availability of a slot
    }

    private void checkWaitingList(){
        while (!waitingList.isEmpty() && availableSlots > 0) {
            Car car = waitingList.remove(0);
            if (bookslot(car, car.chargingDuration())) {
                System.out.println(car.ID() + " moved from the waiting list and got a slot.");
            }
        }
    }
}
class Car extends Thread {
    private String ID;
    private int location;
    private int chargingDuration;
    private ChargingStation[] stations;
    private ChargingStation nearestStation;

    public Car(String ID, int location, int chargingDuration, ChargingStation[] stations) {
        this.ID = ID;
        this.location = location;
        this.chargingDuration = chargingDuration;
        this.stations = stations;
        this.nearestStation = findNearestStation();
    }

    public ChargingStation findNearestStation() {
        int minDistance = Integer.MAX_VALUE;
        ChargingStation nearest = null;

        for (ChargingStation station : stations) {
            int distance = Math.abs(station.location - this.location);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = station;
            }
        }
        //System.out.println("Nearest Station for " + this.ID() + " is at location " + nearest.location);
        return nearest;
    }

    public String ID() {
        return ID;
    }

    public int chargingDuration() {
        return chargingDuration;
    }

    public void run() {
        if (nearestStation.bookslot(this, chargingDuration)) {
            try {
                Thread.sleep(chargingDuration * 1000); // Simulating charging duration in seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nearestStation.releaseSlot(this);

        }
    }}
public class Main {

    public static void main(String[] args) throws IOException {
        String currentDirectory = System.getProperty("user.dir");
        String newDirectoryName = "StationLogFiles";
        File newDirectory = new File(currentDirectory, newDirectoryName);
        if (!newDirectory.exists()) {
            if (newDirectory.mkdir()) {
                System.out.println("Directory created: " + newDirectory.getAbsolutePath());
            } else {
                System.out.println("Failed to create directory.");
                return;
            }
        }
        File s1file=new File(newDirectory,"Station1LogFiles");
        File s2file=new File(newDirectory,"Station2LogFiles");
        try {
            if(s1file.createNewFile()){
                System.out.println("Station 1 Log files created");
            }
            
        } catch (IOException e) {
            System.out.println("Error Occured in creating Station 1 Log files");
            e.printStackTrace();
        }
        try {
            if(s2file.createNewFile()){
                System.out.println("Station 2 Log files created");
            }
            
        } catch (IOException e) {
            System.out.println("Error Occured in creating Station 2 Log files");
            e.printStackTrace();
        }
        FileWriter f1=new FileWriter(s1file.getAbsolutePath());
        FileWriter f2 = new FileWriter(s2file.getAbsolutePath());
        System.out.println(s1file.getAbsolutePath());

        ChargingStation station1 = new ChargingStation(1, 1, 2,f1); // Create charging stations
        ChargingStation station2 = new ChargingStation(2, 7, 2,f2);

        ChargingStation[] stations = {station1, station2}; // Create an array of charging stations

        // Create cars with different charging durations and locations
        Car car1 = new Car("car1", 1, 3, stations);
        Car car2 = new Car("car2", 2, 2, stations);
        Car car3 = new Car("car3", 3, 4, stations);
        Car car4 = new Car("car4", 4, 1, stations);
        Car car5 = new Car("car5", 5,3, stations);
        Car car6 = new Car("car6",6, 2, stations);
        Car car7 = new Car("car7",7, 4, stations);
        Car car8 = new Car("car8",8, 1, stations);

        // Start threads for each car
        car1.start();
        car2.start();
        car3.start();
        car4.start();
        car5.start();
        car6.start();
        car7.start();
        car8.start();

        // Continuously check if any car threads are active
        while (car1.isAlive() || car2.isAlive() || car3.isAlive() || car4.isAlive()||car5.isAlive() || car6.isAlive() || car7.isAlive() || car8.isAlive()) {
            try {
                Thread.sleep(1000); // Sleep for a short duration before rechecking
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

      
        // f1.flush();
        // f2.flush();
        f1.close();
        f2.close();
        System.out.println("All cars have finished their charging. Program terminated.");
        System.out.println("=========================================================================================================");
    }
}
