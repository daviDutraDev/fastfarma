import { Link } from "react-router-dom";
import logo from "../../assets/fastfarma-logo.png";
import "./SidebarBrand.css";

export default function SidebarBrand({ to }) {
    return (
        <Link to={to} className="sidebar-logo sidebar-brand" aria-label="FastFarma — início">
            <img src={logo} alt="FastFarma" width="210" height="70" />
        </Link>
    );
}
