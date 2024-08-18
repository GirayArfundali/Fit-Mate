package com.example.spor

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseConnection {

    // PostgreSQL bağlantı bilgileri
    private val dbUrl = "jdbc:postgresql://192.168.1.104:5432/fıtmate"
    private val dbUser = "postgres"
    private val dbPassword = "47316699"

    // PostgreSQL bağlantısını kur
    fun connectToPostgreSQL(callback: (Connection?) -> Unit) {
        Thread {
            var connection: Connection? = null
            try {
                Class.forName("org.postgresql.Driver")
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
                println("PostgreSQL bağlantısı başarıyla kuruldu.")
            } catch (e: SQLException) {
                println("PostgreSQL bağlantısı kurulurken SQL hatası oluştu: ${e.message}")
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                println("PostgreSQL sürücüsü bulunamadı: ${e.message}")
                e.printStackTrace()
            } catch (e: Exception) {
                println("Bilinmeyen bir hata oluştu: ${e.message}")
                e.printStackTrace()
            }
            callback(connection)
        }.start()
    }

    fun registerUser(
        username: String,
        password: String,
        age: String,
        height: String,
        weight: String,
        email: String,
        phoneNumber: String,
        callback: (Boolean) -> Unit
    ) {
        connectToPostgreSQL { connection ->
            if (connection != null) {
                try {
                    val statement = connection.createStatement()
                    val sql =
                        "INSERT INTO users (name, password, age, height, weight, email, phone) VALUES ('$username', '$password', '$age', '$height', '$weight', '$email', '$phoneNumber')"
                    val rowsAffected = statement.executeUpdate(sql)
                    if (rowsAffected > 0) {
                        println("Kullanıcı bilgileri başarıyla kaydedildi.")
                        callback(true)
                    } else {
                        println("Kullanıcı bilgileri kaydedilemedi.")
                        callback(false)
                    }
                } catch (e: SQLException) {
                    println("Kullanıcı kaydedilirken SQL hatası oluştu: ${e.message}")
                    e.printStackTrace()
                    callback(false)
                } finally {
                    try {
                        connection.close()
                    } catch (e: SQLException) {
                        println("Veritabanı bağlantısı kapatılırken hata oluştu: ${e.message}")
                        e.printStackTrace()
                    }
                }
            } else {
                callback(false)
            }
        }
    }


    fun authenticateUser(email: String, password: String, callback: (Boolean) -> Unit) {
        connectToPostgreSQL { connection ->
            if (connection != null) {
                val query = "SELECT * FROM users WHERE email = ? AND password = ?"

                try {
                    val preparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, email)
                    preparedStatement.setString(2, password)

                    val resultSet = preparedStatement.executeQuery()

                    val userExists = resultSet.next()
                    callback(userExists)
                } catch (e: SQLException) {
                    println("Kullanıcı doğrulama sırasında hata oluştu: ${e.message}")
                    callback(false)
                }
            } else {
                println("Veritabanı bağlantısı kurulamadı.")
                callback(false)
            }
        }
    }

    fun getUserDetails(email: String, callback: (UserDetails?) -> Unit) {
        connectToPostgreSQL { connection ->
            if (connection != null) {
                val query = "SELECT * FROM users WHERE email = ?"
                try {
                    val preparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, email)
                    val resultSet = preparedStatement.executeQuery()

                    if (resultSet.next()) {
                        val name = resultSet.getString("name")
                        val age = resultSet.getInt("age")
                        val height = resultSet.getFloat("height")
                        val weight = resultSet.getFloat("weight")
                        val email = resultSet.getString("email")
                        val phoneNumber = resultSet.getString("phone")
                        val userDetails = UserDetails(name, age, height, weight, email, phoneNumber)
                        callback(userDetails)
                    } else {
                        callback(null) // Kullanıcı bulunamadı
                    }
                } catch (e: SQLException) {
                    println("Kullanıcı bilgilerini alırken hata oluştu: ${e.message}")
                    callback(null)
                }
            } else {
                println("Veritabanı bağlantısı kurulamadı.")
                callback(null)
            }
        }
    }

    fun checkUserExists(email: String, callback: (Boolean) -> Unit) {
        connectToPostgreSQL { connection ->
            if (connection != null) {
                val query = "SELECT * FROM users WHERE email = ?"

                try {
                    val preparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, email)
                    val resultSet = preparedStatement.executeQuery()

                    val userExists = resultSet.next()
                    callback(userExists)
                } catch (e: SQLException) {
                    println("Kullanıcı doğrulama sırasında hata oluştu: ${e.message}")
                    callback(false)
                }
            } else {
                println("Veritabanı bağlantısı kurulamadı.")
                callback(false)
            }
        }
    }

    fun changePassword(email: String, newPassword: String, callback: (Boolean) -> Unit) {
        connectToPostgreSQL { connection ->
            if (connection != null) {
                val query = "UPDATE users SET password = ? WHERE email = ?"

                try {
                    val preparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, newPassword)
                    preparedStatement.setString(2, email)

                    val rowsAffected = preparedStatement.executeUpdate()

                    if (rowsAffected > 0) {
                        callback(true) // Password updated successfully
                    } else {
                        callback(false) // No rows affected, possibly user not found
                    }
                } catch (e: SQLException) {
                    println("Error occurred during password change: ${e.message}")
                    callback(false)
                }
            } else {
                println("Database connection could not be established.")
                callback(false)
            }
        }
    }
    fun updateUserDetails(userDetails: UserDetails, callback: (Boolean) -> Unit) {
        connectToPostgreSQL { connection ->
            if (connection != null) {
                val query = "UPDATE users SET name = ?, age = ?, height = ?, weight = ?, email = ?, phone = ? WHERE email = ?"
                try {
                    val preparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, userDetails.name)
                    preparedStatement.setInt(2, userDetails.age)
                    preparedStatement.setFloat(3, userDetails.height)
                    preparedStatement.setFloat(4, userDetails.weight)
                    preparedStatement.setString(5, userDetails.email)
                    preparedStatement.setString(6, userDetails.phoneNumber)
                    preparedStatement.setString(7, userDetails.email)

                    val rowsAffected = preparedStatement.executeUpdate()
                    callback(rowsAffected > 0) // true if update was successful
                } catch (e: SQLException) {
                    println("Error updating user details: ${e.message}")
                    callback(false)
                }
            } else {
                println("Database connection could not be established.")
                callback(false)
            }
        }
    }
    fun updateProfileImage(email: String, image: ByteArray, callback: (Boolean) -> Unit) {
        connectToPostgreSQL { connection ->
            if (connection != null) {
                val query = "UPDATE users SET image = ? WHERE email = ?"
                try {
                    val preparedStatement = connection.prepareStatement(query)
                    preparedStatement.setBytes(1, image)
                    preparedStatement.setString(2, email)

                    val rowsAffected = preparedStatement.executeUpdate()
                    callback(rowsAffected > 0) // true if update was successful
                } catch (e: SQLException) {
                    println("Error occurred while updating profile image: ${e.message}")
                    callback(false)
                }
            } else {
                println("Database connection could not be established.")
                callback(false)
            }
        }
    }

    // Get user profile image
    fun getUserImage(email: String, callback: (ByteArray?) -> Unit) {
        connectToPostgreSQL { connection ->
            if (connection != null) {
                val query = "SELECT image FROM users WHERE email = ?"
                try {
                    val preparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, email)
                    val resultSet = preparedStatement.executeQuery()

                    if (resultSet.next()) {
                        val imageByteArray = resultSet.getBytes("image")
                        callback(imageByteArray)
                    } else {
                        callback(null) // User not found or no image
                    }
                } catch (e: SQLException) {
                    println("Error occurred while fetching user image: ${e.message}")
                    callback(null)
                }
            } else {
                println("Database connection could not be established.")
                callback(null)
            }
        }
    }
    fun saveStepAndCalories(email: String, steps: Int, calories: Float, date: java.sql.Date, callback: (Boolean) -> Unit) {
        connectToPostgreSQL { connection ->
            if (connection != null) {
                try {
                    // Önce mevcut kullanıcı verilerini kontrol ediyoruz
                    val checkQuery = "SELECT * FROM daily_data WHERE email = ? AND date = ?"
                    val checkStatement = connection.prepareStatement(checkQuery)
                    checkStatement.setString(1, email)
                    checkStatement.setDate(2, date)
                    val resultSet = checkStatement.executeQuery()

                    if (resultSet.next()) {
                        // Eğer bu tarihe ait veri varsa, güncelleme yapılıyor
                        val updateQuery = "UPDATE daily_data SET steps = ?, calories = ? WHERE email = ? AND date = ?"
                        val updateStatement = connection.prepareStatement(updateQuery)
                        updateStatement.setInt(1, steps)
                        updateStatement.setFloat(2, calories)
                        updateStatement.setString(3, email)
                        updateStatement.setDate(4, date)

                        val rowsAffected = updateStatement.executeUpdate()
                        callback(rowsAffected > 0) // true if update was successful
                    } else {
                        // Eğer bu tarihe ait veri yoksa, yeni veri ekleniyor
                        val insertQuery = "INSERT INTO daily_data (email, steps, calories, date) VALUES (?, ?, ?, ?)"
                        val insertStatement = connection.prepareStatement(insertQuery)
                        insertStatement.setString(1, email)
                        insertStatement.setInt(2, steps)
                        insertStatement.setFloat(3, calories)
                        insertStatement.setDate(4, date)

                        val rowsAffected = insertStatement.executeUpdate()
                        callback(rowsAffected > 0) // true if insertion was successful
                    }

                } catch (e: SQLException) {
                    println("Error occurred while saving steps and calories: ${e.message}")
                    callback(false)
                }
            } else {
                println("Database connection could not be established.")
                callback(false)
            }
        }
    }



}

