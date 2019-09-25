/* This file was generated by SableCC (http://www.sablecc.org/). */

package node;

import analysis.*;

@SuppressWarnings("nls")
public final class ANegativeTerm extends PTerm
{
    private TMinus _minus_;
    private TNumber _number_;

    public ANegativeTerm()
    {
        // Constructor
    }

    public ANegativeTerm(
        @SuppressWarnings("hiding") TMinus _minus_,
        @SuppressWarnings("hiding") TNumber _number_)
    {
        // Constructor
        setMinus(_minus_);

        setNumber(_number_);

    }

    @Override
    public Object clone()
    {
        return new ANegativeTerm(
            cloneNode(this._minus_),
            cloneNode(this._number_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseANegativeTerm(this);
    }

    public TMinus getMinus()
    {
        return this._minus_;
    }

    public void setMinus(TMinus node)
    {
        if(this._minus_ != null)
        {
            this._minus_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._minus_ = node;
    }

    public TNumber getNumber()
    {
        return this._number_;
    }

    public void setNumber(TNumber node)
    {
        if(this._number_ != null)
        {
            this._number_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._number_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._minus_)
            + toString(this._number_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._minus_ == child)
        {
            this._minus_ = null;
            return;
        }

        if(this._number_ == child)
        {
            this._number_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._minus_ == oldChild)
        {
            setMinus((TMinus) newChild);
            return;
        }

        if(this._number_ == oldChild)
        {
            setNumber((TNumber) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}