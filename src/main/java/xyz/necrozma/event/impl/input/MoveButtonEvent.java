package xyz.necrozma.event.impl.input;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import xyz.necrozma.event.Event;
import xyz.necrozma.event.impl.Button;


@Getter
@Setter
@AllArgsConstructor
public final class MoveButtonEvent extends Event {
    private Button left;
    private Button right;
    private Button backward;
    private Button forward;
    private boolean sneak;
    private boolean jump;

    public void setForward(final boolean forward) {
        this.getForward().setButton(forward);
    }

    public void setBackward(final boolean backward) {
        this.getBackward().setButton(backward);
    }

    public void setLeft(final boolean left) {
        this.getLeft().setButton(left);
    }

    public void setRight(final boolean right) {
        this.getRight().setButton(right);
    }
}
