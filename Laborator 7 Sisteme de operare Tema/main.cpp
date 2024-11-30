#include <iostream>
#include <thread>
#include <queue>
#include <condition_variable>
#include <mutex>
#include <string>
#include <chrono>
#include <vector>

class ResourceAccess {
private:
    std::mutex mtx; 
    std::condition_variable whiteCondition;
    std::condition_variable blackCondition;
    int whiteCount = 0; 
    int blackCount = 0; 
    bool whiteTurn = true; 
    std::queue<std::pair<std::string, int>> requestQueue; 

    
    void useResource(const std::string& color, int index) const {
        std::cout << color << " thread " << index << " is using the resource.\n";
        std::this_thread::sleep_for(std::chrono::seconds(1)); 
        std::cout << color << " thread " << index << " finished using the resource.\n";
    }

public:
    
    void accessResource(const std::string& color, int index) {
        std::unique_lock<std::mutex> lock(mtx);
        requestQueue.emplace(color, index);

        if (color == "white") {
            while (blackCount > 0 || !whiteTurn || requestQueue.front().first != "white") {
                whiteCondition.wait(lock);
            }
            ++whiteCount;
        }
        else if (color == "black") {
            while (whiteCount > 0 || whiteTurn || requestQueue.front().first != "black") {
                blackCondition.wait(lock);
            }
            ++blackCount;
        }

        requestQueue.pop(); 
        lock.unlock();

        
        useResource(color, index);

        lock.lock();
        if (color == "white") {
            --whiteCount;
            if (whiteCount == 0) {
                whiteTurn = false; 
                blackCondition.notify_all();
            }
        }
        else if (color == "black") {
            --blackCount;
            if (blackCount == 0) {
                whiteTurn = true; 
                whiteCondition.notify_all();
            }
        }
    }
};


void resourceThread(ResourceAccess& resourceAccess, const std::string& color, int index) {
    resourceAccess.accessResource(color, index);
}

int main() {
    ResourceAccess resourceAccess;

    
    std::vector<std::thread> threads;
    int threadIndex = 1;

    for (int i = 0; i < 5; ++i) {
        threads.emplace_back(resourceThread, std::ref(resourceAccess), "white", threadIndex++);
        threads.emplace_back(resourceThread, std::ref(resourceAccess), "black", threadIndex++);
    }

    
    for (auto& thread : threads) {
        thread.join();
    }

    return 0;
}
