import classNames from "classnames";
import { To, useNavigate } from "react-router-dom";

type Props = {
    to: To;
    text: string,
    isActive: boolean
}

export const SideNavButton = (props: Props) => {
    const {to, text, isActive} = props;
    const navigate = useNavigate();
    
    return (<button className={classNames(
        "w-[90%] h-[60px] rounded-full text-l border-4",
        "font-semibold",
        "px-4",
        "my-2",
        "hover:text-[#4393c9] ", 
        "hover:bg-[#082150] duration-300", 
        "border-[#082150]",
        { "text-[#0f2057] bg-[#4393c9]": !isActive },
        { "text-[#4393c9] bg-[#082150]" : isActive },
    )}
        onClick={() => navigate(to)}>
        {text}
    </button>)
}