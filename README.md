# 🦷 Balanced Dental Lab Management System

> 一套為牙技所設計的 **全端管理系統**，整合病例、訂單、產品、客戶、帳號與報表模組。  
> 本專案採用 **Spring Boot + MySQL + Vue.js + Bootstrap**，並搭配 Chart.js 做視覺化統計。  


---

## 📚 目錄
- [專案簡介](#-專案簡介)
- [技術架構](#-技術架構)
- [系統架構設計](#-系統架構設計)
- [資料庫結構 (Schema)](#-資料庫結構-schema)
- [主要功能模組](#-主要功能模組)
  - [病例與訂單管理](#1--病例與訂單管理)
  - [診所與醫師管理](#2-診所與醫師管理)
  - [產品管理](#3-產品管理)
  - [會員與權限控管](#4-會員與權限控管)
  - [Dashboard 與報表](#5-dashboard-與報表)
- [API 結構概覽](#-api-結構概覽)
- [安裝與執行](#-安裝與執行)
- [測試帳號](#-測試帳號)
- [專案亮點與難點](#-專案亮點與難點)
- [我在這個專案中學到的](#-我在這個專案中學到的)
- [未來可擴充方向](#-未來可擴充方向)
- [授權 License](#-授權-license)

---

## 🧭 專案簡介

Balance Dental Lab 是一個牙技所日常營運的資訊化系統，涵蓋以下核心功能：

- 🧾 **病例與訂單管理**（多產品品項支援）
- 🏥 **診所與醫師資料管理**（多對多對應）
- 🦷 **產品類別 / 項目 / 明細與價格設定**
- 👤 **會員與權限控管（Admin / Staff）**
- 📊 **營收統計與 KPI 視覺化報表**

📸 *建議截圖 1：專案首頁 Dashboard 畫面（顯示 KPI 與圓餅圖）*

---

## 🧰 技術架構

| 區塊 | 技術 |
|------|------|
| 前端 | HTML、Vue.js (CDN)、Bootstrap 5、Bootstrap Icons、Chart.js |
| 後端 | Spring Boot 3.x、Spring Data JPA、Maven |
| 資料庫 | MySQL 8.0 |
| 通訊 | RESTful API、CORS |
| 版本控管 | Git / GitHub |

📸 *建議截圖 2：專案分層結構圖（或 IntelliJ 專案樹）*

---

## 🏗️ 系統架構設計

```
Frontend (HTML + Vue + Bootstrap)
           ↓
Controller (Spring Boot REST API)
           ↓
Service (Business Logic)
           ↓
Repository (Spring Data JPA)
           ↓
Entity (ORM)
           ↓
MySQL (RDBMS)
```

- **前端**：以 Vue.js + Bootstrap 建立互動式表單與即時預覽，搭配 Chart.js 呈現統計資料。
- **後端**：以 Spring Boot 分層架構（Controller / Service / Repository）實作。
- **資料庫**：以 MySQL 儲存病例、產品、客戶、訂單資料，並有完整外鍵與多對多關聯設計。

---

## 🧱 資料庫結構 (Schema)

| 模組 | 主要資料表 | 關聯 |
|------|------------|------|
| 病例/訂單 | `case`、`order`、`order_detail` | 一對多 |
| 產品管理 | `product_category`、`product_item`、`product_detail` | 一對多 |
| 客戶資料 | `clinic`、`dentist`、`clinic_dentist` | 多對多 |
| 會員/權限 | `member` | - |

📸 *建議截圖 3：ERD 圖或資料表關聯圖（可用 draw.io 或 IntelliJ Database 生成）*

---

## 📌 主要功能模組

### 1. 🧾 病例與訂單管理
- 建立病例時可同時加入多筆產品明細（含類別、品項、齒位、數量）
- 自動計算金額，連動資料庫 `order` 與 `order_detail`
- 提供一般件 / 急件分類統計

📸 *建議截圖 4：建立病例頁面（含多品項區塊）*

---

### 2. 🏥 診所與醫師管理
- 診所與醫師為多對多關係（`clinic_dentist`）
- 建立診所後可關聯多位醫師
- 支援搜尋與編輯

📸 *建議截圖 5：診所／醫師管理畫面*

---

### 3. 🦷 產品管理
- 產品類別 → 項目 → 明細（價格）
- 用於病例開單時的產品選項來源
- 資料來源：`product_category`、`product_item`、`product_detail`

📸 *建議截圖 6：產品管理畫面*

---

### 4. 👤 會員與權限控管
- 帳號角色分為：
  - `admin`：可進行所有操作
  - `staff`：瀏覽／有限操作
- 使用 Guard Overlay 進行前端權限防護

📸 *建議截圖 7：權限警示彈窗*

---

### 5. 📊 Dashboard 與報表
- 今日新增訂單、一般件、急件 KPI
- 診所營收佔比圓餅圖（可切換時間區間）
- 即時顯示營運狀況

📸 *建議截圖 8：Dashboard KPI 與圓餅圖畫面*

---

## 🧭 API 結構概覽

| 模組 | Controller | Service | Repository |
|------|------------|---------|------------|
| 登入／會員 | `MemberLoginController` / `MemberController` | `MemberLoginService` / `MemberService` | `MemberRepository` |
| 診所／醫師 | `ClinicController` / `DentistController` / `ClinicDentistController` | `ClinicService` / `DentistService` / `ClinicDentistService` | `ClinicRepository` 等 |
| 產品管理 | `ProductCategoryController` / `ProductItemController` / `ProductDetailController` | `Product*Service` | `Product*Repository` |
| 病例／訂單 | `OrderController` / `CaseController` | `OrderService` / `CaseService` | `OrderRepository` / `CaseRepository` |
| 報表 | `ReportController` | `ReportService` | - |

---

## 📌 專案亮點與難點

- 💡 **多對多關聯實作**（診所與醫師）  
- 🔄 **病例與訂單同步建立流程**（含多筆產品品項）  
- 🔐 **權限管理與前端 Guard Overlay**  
- 📊 **Chart.js 報表呈現與後端統計整合**  
- 🧭 **乾淨的分層架構（Controller / Service / Repository）**

---

## 🧠 我在這個專案中學到的

- 規劃資料表關聯與前後端資料流的整合思維  
- Spring Boot REST API 設計與 Vue.js 前端資料綁定  
- 如何用清晰的資料結構支撐業務邏輯  
- 在前端加入權限控管與使用者體驗細節

---

## 🏁 未來可擴充方向

- ✅ API token 登入驗證
- ✅ Docker Compose 自動化部署
- ✅ RWD 手機版操作介面

---

✅ **小結**：這份專案完整呈現了我作為一名轉職工程師對於全端開發的理解能力，  
從資料庫設計 → 後端邏輯 → 前端互動 → 權限控管 → 報表統計，  
所有功能皆由我獨立開發完成，並以工程師的角度整理為乾淨且可維護的架構。
