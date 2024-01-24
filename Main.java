import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    }
}
