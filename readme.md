# People Manager - High-Performance Redis Cache

This project implements a **high-performance caching layer** for managing people using **Redis** with **Spring Boot** and **Spring Data Redis**.  
It is designed to handle **fast writes and reads** through **double-buffered caching**, **pipeline operations**, and **parallel batch processing**.

---

## ⚡ Key Features

- **High-Performance Caching**
    - Uses **Redis pipelines** to perform batch writes with minimal network overhead.
    - Utilizes **ForkJoinPool** to process data in parallel for faster cache population.
    - Optimized for both **high write throughput** (during cache population) and **fast read operations**.

- **Double Buffering Strategy**
    - Two Redis databases (DB1 and DB2) are used.
    - One database is **active for reads**, while the other is **populated in the background**.
    - Once fully populated, the inactive DB becomes active by updating the control key `cache:activeDb`.
    - This ensures **zero downtime and atomic cache swaps**.

- **Structured Redis Keys**
    - `person_{id}_{cpf}` → Full person data in JSON
    - `all_people` → Set containing all person keys
    - `people_by_city_{city}`, `people_by_state_{state}`, `people_by_country_{country}` → Index sets for fast filtering
    - `person_by_cpf_{cpf}` → Set linking CPF to the person key

- **Scheduled Cache Refresh**
    - Periodically clears the inactive DB.
    - Fetches all people from the database.
    - Writes data in **batches of 100** using **Redis pipelines** for maximum throughput.
    - Activates the newly populated DB seamlessly.

---

## 🔹 Example Key Structure

```text
cache:activeDb -> 1
person_1_12345678900 -> { "id": 1, "cpf": "12345678900", ... }
people_by_city_New York -> { "person_1_12345678900", ... }
person_by_cpf_12345678900 -> { "person_1_12345678900" }