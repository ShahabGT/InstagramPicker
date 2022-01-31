
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val mutableResult = MutableLiveData<List<String>>()
    val result: LiveData<List<String>> get() = mutableResult

    fun setResult(date: List<String>) {
        mutableResult.value = date
    }



}