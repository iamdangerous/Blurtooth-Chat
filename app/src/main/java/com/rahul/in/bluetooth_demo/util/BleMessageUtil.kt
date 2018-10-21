package com.rahul.`in`.bluetooth_demo.util

import com.rahul.`in`.bluetooth_demo.bleControllers.BleMeshController
import com.rahul.`in`.bluetooth_demo.bleControllers.BleMeshController.Companion.DEFAULT_CHUNK_SIZE
import org.json.JSONObject
import timber.log.Timber
import java.nio.charset.Charset
import java.util.*

class BleMessageUtil {

    companion object {
        fun stringToListOfByteArray(text: String, chunkSize: Int = DEFAULT_CHUNK_SIZE): List<ByteArray> {

            fun printTillHere(mutableList: MutableList<Byte>) {
                val array = ByteArray(mutableList.size)
                val a = (0 until mutableList.size).forEach {
                    array[it] = mutableList[it]
                }
                val formattedString = array.toString(Charset.defaultCharset())
                Timber.d("formatted = $formattedString")
            }

            println("original = $text")
            val divide = chunkSize - 2
            var bytes = text.toByteArray(Charset.defaultCharset())
            val mutableList = bytes.toMutableList()

            var reminder = text.length % divide
            var breakInto = 0
            if (reminder == 0) {
                breakInto = text.length / divide
            } else {
                breakInto = ((text.length - reminder) / divide) + 1
            }
            var insertPosForLastItem = ((breakInto - 1) * divide) + ((breakInto - 1) * 2)
            var writeInBetween = 0
            Timber.d("insertPosForLastItem = $insertPosForLastItem")
            var it = 0
            while (it < mutableList.size && it >= 0) {
                if (it == insertPosForLastItem) {
                    Timber.d("1")
                    printTillHere(mutableList)

                    mutableList.add(it, 's'.toByte())
                    mutableList.add(it + 1, '3'.toByte())

                    Timber.d("2")
                    printTillHere(mutableList)
                    break
                } else if (it == 0) {
                    mutableList.add(it, 's'.toByte())
                    mutableList.add(it + 1, '1'.toByte())
                } else if (it % chunkSize == 0 && writeInBetween < (breakInto - 2)) {
                    mutableList.add(it, 's'.toByte())
                    mutableList.add(it + 1, '2'.toByte())
                    writeInBetween = writeInBetween + 1
                }
                it += 1
            }
            println(mutableList.size)
            val array = ByteArray(mutableList.size)
            val a = (0 until mutableList.size).forEach {
                array[it] = mutableList[it]
            }
            val formattedString = array.toString(Charset.defaultCharset())
            println("formatted = $formattedString")


            //Split byte array into 5 chunks
            val listOfOriginalBytes = arrayListOf<ByteArray>()

            var index1 = 0
            var startIndex = -chunkSize
            var endIndex = 0
            while (index1 < array.size) {
                startIndex = startIndex + chunkSize
                endIndex = endIndex + chunkSize
                if (endIndex > array.size) {
                    listOfOriginalBytes.add(array.sliceArray(startIndex until array.size))
                    break
                } else {
                    listOfOriginalBytes.add(array.sliceArray(startIndex until endIndex))
                }

            }
            return listOfOriginalBytes
        }

        fun listOfByteArrayToString(listOfOriginalBytes: ArrayList<ByteArray>, chunkSize: Int = DEFAULT_CHUNK_SIZE): String {
            val byteArraySize = (chunkSize * (listOfOriginalBytes.size - 1)) + listOfOriginalBytes[listOfOriginalBytes.size - 1].size
            val receiverByteArray = ByteArray(byteArraySize)

            var bIndex = 0
            for (byteArray in listOfOriginalBytes) {
                for (byte in byteArray) {
                    receiverByteArray[bIndex] = byte
                    bIndex = bIndex + 1
                }
            }

            //we will get listOfOriginalBytes at our o/p
            val mutableListNew = receiverByteArray.toMutableList()
            var it = 0
            while (it < mutableListNew.size && it >= 0) {
                if (it == 0) {
                    mutableListNew.removeAt(0)
                    mutableListNew.removeAt(0)
                } else if (it % (chunkSize - 2) == 0 && ((it + 1) < mutableListNew.size - 1)) {
                    mutableListNew.removeAt(it)
                    mutableListNew.removeAt(it)
                }
                it += 1
            }

            val arrayNew = ByteArray(mutableListNew.size)
            val aNew = (0 until mutableListNew.size).forEach {
                arrayNew[it] = mutableListNew[it]
            }
            val formattedStringNew = arrayNew.toString(Charset.defaultCharset())
            println("formattedNew = $formattedStringNew")
            return formattedStringNew
        }

        fun byteArrayToMessageQueueData(byteArray: ByteArray, characteristicUuid: UUID, bleAddress: String = ""): BleMeshController.MessageQueueData {
            return BleMeshController.MessageQueueData(bleAddress, byteArray.toString(Charset.defaultCharset()), byteArray, byteArray[1].toInt(), characteristicUuid)
        }

        fun parseInboxDataWithUUID(listOfBytes: ArrayList<ByteArray>, characteristicUUID: UUID, chunkSize: Int = DEFAULT_CHUNK_SIZE) {
            val string = listOfByteArrayToString(listOfBytes, chunkSize)

            if (characteristicUUID == BleMeshController.USER_META_DATA_UUID) {
                //parse meta data

                val json = JSONObject(string)
                val name = json.getString("n")
                val userName = json.getString("un")
                val email = json.getString("e")
//                val avatarUrl = json.getString("au")

                Timber.d("name = $name")
                Timber.d("userName = $userName")
                Timber.d("email = $email")
//                Timber.d("avatarUrl = $avatarUrl")

            } else if (characteristicUUID == BleMeshController.ONE_TO_ONE_MSG_UUID) {

            }
        }
    }

}