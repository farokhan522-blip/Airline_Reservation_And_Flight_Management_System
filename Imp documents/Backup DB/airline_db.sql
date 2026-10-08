-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 20, 2026 at 09:05 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `airline_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `adminid` int(11) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`adminid`, `password`, `username`) VALUES
(1, 'admin123', 'admin');

-- --------------------------------------------------------

--
-- Table structure for table `airline`
--

CREATE TABLE `airline` (
  `airlineid` varchar(255) NOT NULL,
  `country` varchar(255) DEFAULT NULL,
  `meal_policy` enum('FREE','NOT_AVAILABLE','PAID') DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `airline`
--

INSERT INTO `airline` (`airlineid`, `country`, `meal_policy`, `name`) VALUES
('AB', 'Pakistan', 'FREE', 'AIRBLUE'),
('PIA', 'Pakistan', NULL, 'pakistan International Airline'),
('QAI', 'Qatar', NULL, 'Qatar Airways'),
('UAE', 'Dubai', NULL, 'Emirates');

-- --------------------------------------------------------

--
-- Table structure for table `booking`
--

CREATE TABLE `booking` (
  `bookingid` int(11) NOT NULL,
  `booking_date` varchar(255) DEFAULT NULL,
  `status` enum('CANCELLED','CONFIRMED','PENDING','WAITING') DEFAULT NULL,
  `flight_id` varchar(255) DEFAULT NULL,
  `owner_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `booking`
--

INSERT INTO `booking` (`bookingid`, `booking_date`, `status`, `flight_id`, `owner_id`) VALUES
(1, '2026-01-11', 'CONFIRMED', 'UK-904', 2),
(2, '2026-01-11', 'CONFIRMED', 'UK-904', 1),
(3, '2026-01-18', 'CONFIRMED', 'IS-904', 2),
(4, '2026-01-19', 'CONFIRMED', 'IS-904', 2),
(5, '2026-01-19', 'CONFIRMED', 'IS-904', 2);

-- --------------------------------------------------------

--
-- Table structure for table `booking_passenger`
--

CREATE TABLE `booking_passenger` (
  `id` int(11) NOT NULL,
  `baggage_weight` double NOT NULL,
  `meal_selected` enum('NONE','NON_VEG','VEG') DEFAULT NULL,
  `seat_no` varchar(255) DEFAULT NULL,
  `travel_class` enum('BUSINESS','ECONOMY','FIRSTCLASS') DEFAULT NULL,
  `booking_id` int(11) DEFAULT NULL,
  `passenger_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `booking_passenger`
--

INSERT INTO `booking_passenger` (`id`, `baggage_weight`, `meal_selected`, `seat_no`, `travel_class`, `booking_id`, `passenger_id`) VALUES
(1, 50, 'VEG', '1A', 'FIRSTCLASS', 1, 2),
(2, 20, 'VEG', '8F', 'ECONOMY', 1, 3),
(3, 20, 'NON_VEG', 'LAP', 'ECONOMY', 1, 4),
(4, 0, 'VEG', 'LAP', 'ECONOMY', 2, 1),
(5, 20, 'NONE', '6A', 'ECONOMY', 2, 5),
(6, 40, 'VEG', '3A', 'BUSINESS', 3, 2),
(7, 20, 'VEG', '10D', 'ECONOMY', 4, 2),
(8, 20, 'VEG', '10D', 'ECONOMY', 5, 2);

-- --------------------------------------------------------

--
-- Table structure for table `flight`
--

CREATE TABLE `flight` (
  `flight_id` varchar(255) NOT NULL,
  `aircraft_id` varchar(255) DEFAULT NULL,
  `base_price` double DEFAULT NULL,
  `business_baggage` double NOT NULL,
  `business_price_factor` double NOT NULL,
  `capacity` int(11) NOT NULL,
  `date` varchar(255) DEFAULT NULL,
  `destination` varchar(255) DEFAULT NULL,
  `economy_baggage` double NOT NULL,
  `first_class_baggage` double NOT NULL,
  `first_class_price_factor` double NOT NULL,
  `source` varchar(255) DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','DELAYED','SCHEDULED') DEFAULT NULL,
  `time` varchar(255) DEFAULT NULL,
  `airline_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `flight`
--

INSERT INTO `flight` (`flight_id`, `aircraft_id`, `base_price`, `business_baggage`, `business_price_factor`, `capacity`, `date`, `destination`, `economy_baggage`, `first_class_baggage`, `first_class_price_factor`, `source`, `status`, `time`, `airline_id`) VALUES
('1', 'Airbus-A380', 20000, 40, 2.5, 200, '2026-01-03', 'Lahore', 20, 50, 4, 'Gilgit', 'COMPLETED', '11:11', 'PIA'),
('IS-904', 'boeing-077', 20000, 40, 2.5, 100, '2026-06-02', 'Africa', 20, 50, 4, 'Sri-lanka', 'SCHEDULED', '01:00', 'AB'),
('PK-308', 'Airbus-A380', 0, 40, 2.5, 300, '2026-02-15', 'Lahore', 20, 50, 4, 'Islamabad ', 'CANCELLED', '18:05', 'AB'),
('PK-505', 'Airbus-A380', 80000, 40, 2.5, 300, '2026-02-21', 'islamabad', 20, 50, 4, 'lahore', 'COMPLETED', '17:00', 'PIA'),
('RS-906', 'Airbus-A132', 300000, 40, 2.5, 200, '2026-03-25', 'Russia', 20, 50, 4, 'South Africa', 'SCHEDULED', '01:35', 'AB'),
('UK-904', 'boeing-077', 1200, 40, 2.5, 100, '2026-02-04', 'Qatar', 20, 50, 4, 'London', 'COMPLETED', '09:55', 'QAI');

-- --------------------------------------------------------

--
-- Table structure for table `passenger`
--

CREATE TABLE `passenger` (
  `id` int(11) NOT NULL,
  `age` int(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `is_registered` bit(1) NOT NULL,
  `passport_number` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `passenger`
--

INSERT INTO `passenger` (`id`, `age`, `email`, `full_name`, `is_registered`, `passport_number`, `password`, `username`) VALUES
(1, 0, 'farokhan522@gmail.com', 'Muhammad Farooq Adnan Khan', b'1', '1233456789', '123', NULL),
(2, 48, 'ASKH@gmail.com', 'Adnan Shahbaz', b'1', '789456123', '123', NULL),
(3, 24, NULL, 'Muhammad Farooq Adnan Khan', b'0', 'A1234567', NULL, NULL),
(4, 1, NULL, 'Ibrahim ehsan ', b'0', 'B1234567', NULL, NULL),
(5, 24, NULL, 'Arman ALi', b'0', 'B1234567', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `payment`
--

CREATE TABLE `payment` (
  `paymentid` int(11) NOT NULL,
  `amount` double NOT NULL,
  `payment_date` varchar(255) DEFAULT NULL,
  `status` enum('FAILED','PENDING','SUCCESS') DEFAULT NULL,
  `booking_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payment`
--

INSERT INTO `payment` (`paymentid`, `amount`, `payment_date`, `status`, `booking_id`) VALUES
(1, 7260, '2026-01-11', 'PENDING', 1),
(2, 2420, '2026-01-11', 'PENDING', 2),
(3, 50020, '2026-01-18', 'PENDING', 3),
(4, 20020, '2026-01-19', 'PENDING', 4),
(5, 20020, '2026-01-19', 'PENDING', 5);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`adminid`);

--
-- Indexes for table `airline`
--
ALTER TABLE `airline`
  ADD PRIMARY KEY (`airlineid`);

--
-- Indexes for table `booking`
--
ALTER TABLE `booking`
  ADD PRIMARY KEY (`bookingid`),
  ADD KEY `FK546eybei9q7dsna94vryofrbr` (`flight_id`),
  ADD KEY `FKhq508cf7nl54ey6hr59rtbkgu` (`owner_id`);

--
-- Indexes for table `booking_passenger`
--
ALTER TABLE `booking_passenger`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKeqxq6sdsnysqnpgw80gi9scxw` (`booking_id`),
  ADD KEY `FK32ke69uee5mfn3y5fx871neer` (`passenger_id`);

--
-- Indexes for table `flight`
--
ALTER TABLE `flight`
  ADD PRIMARY KEY (`flight_id`),
  ADD KEY `FK37wfh52g7g91rllg104gfq3yv` (`airline_id`);

--
-- Indexes for table `passenger`
--
ALTER TABLE `passenger`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `payment`
--
ALTER TABLE `payment`
  ADD PRIMARY KEY (`paymentid`),
  ADD UNIQUE KEY `UKku02qy6369hn9uhy3n7jk9v6e` (`booking_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admin`
--
ALTER TABLE `admin`
  MODIFY `adminid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `booking`
--
ALTER TABLE `booking`
  MODIFY `bookingid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `booking_passenger`
--
ALTER TABLE `booking_passenger`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `passenger`
--
ALTER TABLE `passenger`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `payment`
--
ALTER TABLE `payment`
  MODIFY `paymentid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `booking`
--
ALTER TABLE `booking`
  ADD CONSTRAINT `FK546eybei9q7dsna94vryofrbr` FOREIGN KEY (`flight_id`) REFERENCES `flight` (`flight_id`),
  ADD CONSTRAINT `FKhq508cf7nl54ey6hr59rtbkgu` FOREIGN KEY (`owner_id`) REFERENCES `passenger` (`id`);

--
-- Constraints for table `booking_passenger`
--
ALTER TABLE `booking_passenger`
  ADD CONSTRAINT `FK32ke69uee5mfn3y5fx871neer` FOREIGN KEY (`passenger_id`) REFERENCES `passenger` (`id`),
  ADD CONSTRAINT `FKeqxq6sdsnysqnpgw80gi9scxw` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`bookingid`);

--
-- Constraints for table `flight`
--
ALTER TABLE `flight`
  ADD CONSTRAINT `FK37wfh52g7g91rllg104gfq3yv` FOREIGN KEY (`airline_id`) REFERENCES `airline` (`airlineid`);

--
-- Constraints for table `payment`
--
ALTER TABLE `payment`
  ADD CONSTRAINT `FKqewrl4xrv9eiad6eab3aoja65` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`bookingid`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
