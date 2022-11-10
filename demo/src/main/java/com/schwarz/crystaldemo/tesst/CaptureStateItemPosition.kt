package schwarz.fwws.shared.model

import com.schwarz.crystalapi.*

@Entity
@Fields(
    Field(name = "here_state", type = CaptureState::class, list = true),
)
open class CaptureStateItemPosition

enum class CaptureState {
    STATE_1,
    STATE_2,
    STATE_3
}
